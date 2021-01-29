package com.breakinblocks.bbserver.module;

import com.breakinblocks.bbserver.util.MiscUtil;
import com.feed_the_beast.mods.ftbbackups.BackupEvent;
import com.feed_the_beast.mods.ftbbackups.Backups;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Only init this if FTB Backups is installed
 */
public final class BackupFreezeFix {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MODULE_ID = "backup_freeze_fix";
    public static final ChunkPos ORIGIN_CHUNK_POS = new ChunkPos(0, 0);
    public static final List<WeakReference<ServerWorld>> forcedWorlds = new ArrayList<>();

    public static boolean isBackupRunning() {
        return Backups.INSTANCE.doingBackup.isRunning();
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        IWorld world = event.getWorld();
        // Chunkload recently loaded dimensions if a backup is ongoing
        if (world instanceof ServerWorld && isBackupRunning())
            forceLoadWorld((ServerWorld) world);
    }

    @SubscribeEvent
    public static void onBackupStart(BackupEvent.Pre event) {
        MiscUtil.sync(() -> {
            // Chunkload all the currently loaded dimensions
            LOGGER.info("Backup Starting. Loading dimensions...");
            for (ServerWorld world : LogicalSidedProvider.INSTANCE.<MinecraftServer>get(LogicalSide.SERVER).getWorlds()) {
                forceLoadWorld(world);
            }
        });
    }

    @SubscribeEvent
    public static void onBackupFinish(BackupEvent.Post event) {
        MiscUtil.sync(() -> {
            forcedWorlds.stream()
                    .map(Reference::get)
                    .filter(Objects::nonNull)
                    .forEach(world -> world.forceChunk(ORIGIN_CHUNK_POS.x, ORIGIN_CHUNK_POS.z, false));
            int num = forcedWorlds.size();
            forcedWorlds.clear();
            LOGGER.info("Backup Finished. Released " + num + " worlds");
        });
    }

    public static void forceLoadWorld(ServerWorld world) {
        if (world.getForcedChunks().contains(ORIGIN_CHUNK_POS.asLong())) return;
        LOGGER.info("Force Loading DIM " + world.getDimensionKey() + ": " + world.getProviderName());
        world.forceChunk(ORIGIN_CHUNK_POS.x, ORIGIN_CHUNK_POS.z, true);
        forcedWorlds.add(new WeakReference<>(world));
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(BackupFreezeFix.class);
    }
}
