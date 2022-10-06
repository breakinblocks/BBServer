package com.breakinblocks.bbserver.module;

import com.breakinblocks.bbserver.BBServerConfig;
import com.breakinblocks.bbserver.util.MiscUtil;
import net.minecraft.server.players.PlayerList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Watches for writes on a file and reloads them
 */
public final class Watcher {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final WatchService watchService;
    private static final ConcurrentHashMap<WatchKey, ConcurrentHashMap<Path, Runnable>> watching = new ConcurrentHashMap<>();
    private static final Timer timer = new Timer("BBServer-WatcherTimer", true);
    private static final ConcurrentHashMap<Runnable, TimerTask> scheduledTasks = new ConcurrentHashMap<>();

    static {
        WatchService watchServiceTemp = null;
        try {
            watchServiceTemp = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            LOGGER.error("Watcher Module failed to create new Watch Service. Files will not be reloaded.");
        }
        watchService = watchServiceTemp;
    }

    private static void watch(File file, Runnable callback) {
        Path path = file.toPath().toAbsolutePath().normalize();
        try {
            WatchKey key = path.getParent().register(watchService, ENTRY_MODIFY);
            watching
                    .computeIfAbsent(key, (k) -> new ConcurrentHashMap<>())
                    .put(path, callback);
            LOGGER.info("Watching " + path.getParent() + " for changes to " + file.getName());
        } catch (IOException e) {
            LOGGER.error("Failed to start watching: " + file.getAbsolutePath(), e);
        }
    }

    private static void threadWatcherService() {
        if (watchService == null) return;
        try {
            while (true) {
                WatchKey key = watchService.take(); // Blocking
                ConcurrentHashMap<Path, Runnable> watchedFiles = watching.get(key);
                List<WatchEvent<?>> events = key.pollEvents();
                //BBServer.log.info("Watch Key " + key.watchable() + " has " + events.size() + " events");
                for (WatchEvent<?> event : events) {
                    if (event.kind() != ENTRY_MODIFY) continue;
                    Path filePath = ((Path) event.context()).toAbsolutePath().normalize();
                    //BBServer.log.info(filePath + " has been modified. Length " + filePath.toFile().length());
                    Runnable yay = watchedFiles.get(filePath);
                    if (yay == null) continue;
                    File file = filePath.toFile();
                    // Sometimes the file is empty then written to later
                    if (filePath.toFile().length() <= 0) continue;
                    //BBServer.log.info(filePath + " has a callback");
                    if (!MiscUtil.getServer().isRunning()) continue;
                    // Schedule or Reschedule
                    TimerTask task = scheduledTasks.compute(yay, (runnable, lastTask) -> {
                        if (lastTask != null)
                            lastTask.cancel();
                        return new TimerTask() {
                            @Override
                            public void run() {
                                runnable.run();
                            }
                        };
                    });
                    timer.schedule(task, BBServerConfig.COMMON.watcher.delay.get() * 1000L);
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            // Exit if interrupted
        } finally {
            // Attempt to close watch service
            try {
                watchService.close();
            } catch (IOException e) {
                // Don't care
            }
        }
    }

    public static void setup() {
        // Dedicated server watchers only (User lists can't be read from file for non-dedicated servers)
        if (!MiscUtil.getServer().isDedicatedServer()) return;

        // Ops reloading
        if (BBServerConfig.COMMON.watcher.ops.get()) watch(PlayerList.OPLIST_FILE, () -> MiscUtil.sync(() -> {
            try {
                PlayerList playerList = MiscUtil.getServer().getPlayerList();
                playerList.getOps().load();
                // Have to update access levels too
                playerList.getPlayers().forEach(playerList::sendPlayerPermissionLevel);
                LOGGER.info("Ops reloaded");
            } catch (IOException e) {
                LOGGER.error("Failed to reload ops", e);
            }
        }));

        // Whitelist reloading
        if (BBServerConfig.COMMON.watcher.whitelist.get()) watch(PlayerList.WHITELIST_FILE, () -> MiscUtil.sync(() -> {
            try {
                MiscUtil.getServer().getPlayerList().getWhiteList().load();
                LOGGER.info("Whitelist reloaded");
            } catch (IOException e) {
                LOGGER.error("Failed to reload whitelist", e);
            }
        }));

        // Start the Watcher thread if there are any watched files
        if (!watching.isEmpty()) {
            final Thread thread = new Thread(Watcher::threadWatcherService, "BBServer-WatcherService");
            thread.setDaemon(true);
            thread.start();
        }
    }
}
