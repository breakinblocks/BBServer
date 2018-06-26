package com.breakinblocks.bbserver.module;

import com.breakinblocks.bbserver.BBServer;
import com.breakinblocks.bbserver.Config;
import com.breakinblocks.bbserver.util.MiscUtil;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Watches for writes on a file
 */
public class Watcher implements Runnable {
    public static final Watcher instance = new Watcher();

    final WatchService watchService;
    final ConcurrentHashMap<WatchKey, ConcurrentHashMap<Path, Runnable>> watching = new ConcurrentHashMap<>();
    final Timer timer = new Timer("BBServer-Watcher", true);

    private Watcher() {
        WatchService watchServiceTemp = null;
        try {
            watchServiceTemp = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            BBServer.log.error("Watcher Module failed to create new Watch Service. Files will not be reloaded.");
        }
        this.watchService = watchServiceTemp;
    }

    public void watch(File file, Runnable callback) {
        Path path = file.toPath().toAbsolutePath().normalize();
        try {
            WatchKey key = path.getParent().register(this.watchService, ENTRY_MODIFY);
            if(!this.watching.containsKey(key)) {
                this.watching.put(key, new ConcurrentHashMap<>());
            }
            this.watching.get(key).put(path, callback);
            BBServer.log.info("Watching "  + path.getParent() + " for changes to " + file.getName());
        } catch (IOException e) {
            BBServer.log.error("Failed to start watching: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public void run() {
        if(this.watchService == null) return;
        try {
            while(true) {
                WatchKey key = this.watchService.take();
                ConcurrentHashMap<Path, Runnable> watchedFiles = this.watching.get(key);
                List<WatchEvent<?>> events = key.pollEvents();
                BBServer.log.info("Watch Key " + key.watchable() + " has " + events.size() + " events");
                for(WatchEvent<?> event : events) {
                    if(event.kind() != ENTRY_MODIFY) continue;
                    Path filePath = ((Path) event.context()).toAbsolutePath().normalize();
                    BBServer.log.info(filePath + " has been modified. Length " + filePath.toFile().length());
                    Runnable yay = watchedFiles.get(filePath);
                    if(yay == null) continue;
                    File file = filePath.toFile();
                    // Sometimes the file is empty then written to later
                    if(filePath.toFile().length() <= 0) continue;
                    BBServer.log.info(filePath + " has a callback");
                    if(!FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning()) continue;
                    yay.run();
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            // Exit if interrupted
        } finally {
            // Attempt to close watch service
            try {
                this.watchService.close();
            } catch (IOException e) {
                // Don't care
            }
        }
    }

    public void setup() {
        // Dedicated server watchers only (User lists can't be read from file for non-dedicated servers)
        if(!FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) return;

        // Ops reloading
        if(Config.Watcher.ops) this.watch(PlayerList.FILE_OPS, () -> MiscUtil.sync(() -> {
            try {
                FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getOppedPlayers().readSavedFile();
                BBServer.log.info("Ops reloaded");
            } catch (IOException e) {
                BBServer.log.error("Failed to reload ops", e);
            }
        }));

        // Whitelist reloading
        if(Config.Watcher.whitelist) this.watch(PlayerList.FILE_WHITELIST, () -> MiscUtil.sync(() -> {
            try {
                FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getWhitelistedPlayers().readSavedFile();
                BBServer.log.info("Whitelist reloaded");
            } catch (IOException e) {
                BBServer.log.error("Failed to reload whitelist", e);
            }
        }));

        // Start the Watcher thread
        Thread thread = new Thread(this.instance, "BBServer-Watcher");
        thread.setDaemon(true);
        thread.start();
    }
}
