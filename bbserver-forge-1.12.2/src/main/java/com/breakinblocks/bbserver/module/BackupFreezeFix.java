package com.breakinblocks.bbserver.module;

import com.breakinblocks.bbserver.BBServer;
import com.breakinblocks.bbserver.BBServerChunkManager;
import com.breakinblocks.bbserver.util.MiscUtil;
import com.feed_the_beast.mods.ftbbackups.BackupEvent;
import com.feed_the_beast.mods.ftbbackups.Backups;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;

/**
 * Only init this if FTB Backups is installed
 */
public final class BackupFreezeFix {

    public static final String MODULE_ID = "backup_freeze_fix";

    public static final HashSet<Ticket> tickets = new HashSet<>();

    public static boolean isBackupRunning() {
        return Backups.INSTANCE.doingBackup > 0;
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        // Chunkload recently loaded dimensions if a backup is ongoing
        if (isBackupRunning())
            forceLoadWorld(event.getWorld());
    }

    @SubscribeEvent
    public static void onBackupStart(BackupEvent.Pre event) {
        MiscUtil.sync(() -> {
            // Chunkload all the currently loaded dimensions
            BBServer.log.info("Backup Starting. Loading dimensions...");
            for (WorldServer world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds) {
                forceLoadWorld(world);
            }
        });
    }

    @SubscribeEvent
    public static void onBackupFinish(BackupEvent.Post event) {
        MiscUtil.sync(() -> {
            tickets.forEach(ForgeChunkManager::releaseTicket);
            int num = tickets.size();
            tickets.clear();
            BBServer.log.info("Backup Finished. Released " + num + " tickets");
        });
    }

    public static void forceLoadWorld(World world) {
        BBServer.log.info("Force Loading DIM" + world.provider.getDimension() + ": " + world.provider);
        Ticket ticket = BBServerChunkManager.requestTicket(MODULE_ID, world, Type.NORMAL);
        ForgeChunkManager.forceChunk(ticket, new ChunkPos(0, 0));
        tickets.add(ticket);
    }

    public static void loadTicket(Ticket ticket, World world) {
        // Discard all tickets since they are meant to be temporary anyway
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(BackupFreezeFix.class);
        BBServerChunkManager.registerHandler(MODULE_ID, BackupFreezeFix::loadTicket);
    }
}
