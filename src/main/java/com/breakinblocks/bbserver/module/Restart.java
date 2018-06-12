package com.breakinblocks.bbserver.module;

import com.breakinblocks.bbserver.BBServer;
import com.breakinblocks.bbserver.Config;
import com.breakinblocks.bbserver.util.ChatUtil;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Mod.EventBusSubscriber(modid = BBServer.MODID)
public class Restart {
    // 2 hours, 1 hour, 30 minutes, 10 minutes, 5 minutes, 1 minutes, 30 seconds, 10 seconds, 5, 4, 3, 2, 1
    static final long[] NOTIFICATIONS = new long[]{2 * 60 * 60, 60 * 60, 30 * 60, 10 * 60, 5 * 60, 60, 30, 10, 5, 4, 3, 2, 1};
    public static boolean restarting = false;
    static long restartTime = -1;
    static int nextRestartMessage = 0;

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (restarting) return;
        if (Config.Restart.mode < 0 || Config.Restart.mode > 1) {
            restarting = true;
            return;
        }

        // Use a consistent "now" throughout this method
        long now = System.currentTimeMillis();

        // Initialisation
        if (restartTime < 0) {
            switch (Config.Restart.mode) {
                case 0: {
                    restartTime = now + (long) (Config.Restart.delay * DateUtils.MILLIS_PER_HOUR);
                }
                break;
                case 1: {
                    // Start of the day for the system timezone
                    long startOfDay = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli();
                    // If no times are specified, will restart in 24 hours
                    restartTime = now + DateUtils.MILLIS_PER_DAY;
                    // Get the closest next restart time
                    for (double restartHour : Config.Restart.times) {
                        long restartMs = startOfDay + (long) (restartHour * DateUtils.MILLIS_PER_HOUR);
                        // If the restart time has already passed, add 24 hours to get the next restart time
                        if (restartMs <= now) {
                            restartMs += DateUtils.MILLIS_PER_DAY;
                        }
                        restartTime = Math.min(restartTime, restartMs);
                    }
                }
                break;
            }

            String message = "Server restarts in " + DurationFormatUtils.formatDurationWords(restartTime - now, true, true);
            FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(
                    ChatUtil.coloredString(message, TextFormatting.LIGHT_PURPLE),
                    true
            );

            long remaining = (restartTime - now) / 1000;
            // Select closest notification
            while (nextRestartMessage < NOTIFICATIONS.length && remaining <= NOTIFICATIONS[nextRestartMessage]) {
                nextRestartMessage++;
            }
        }

        // Seconds remaining
        long remaining = (restartTime - now) / 1000;

        // Restart Notifications
        if (remaining > 0 && nextRestartMessage < NOTIFICATIONS.length && remaining <= NOTIFICATIONS[nextRestartMessage]) {
            // Select next closest notification
            while (nextRestartMessage < NOTIFICATIONS.length && remaining <= NOTIFICATIONS[nextRestartMessage]) {
                nextRestartMessage++;
            }
            // Seconds for current notification
            remaining = NOTIFICATIONS[nextRestartMessage - 1];

            String message = "Server restarts in " + DurationFormatUtils.formatDurationWords(remaining * 1000, true, true);
            FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(
                    ChatUtil.coloredString(message, TextFormatting.LIGHT_PURPLE),
                    true
            );
        }

        // Restart
        if (now > restartTime) {
            try {
                restart();
            } catch (IOException e) {
                BBServer.log.error("Failed to create the restart flag file.");
                FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(
                        ChatUtil.coloredString("Server failed to restart.", TextFormatting.DARK_RED),
                        true
                );
            }
        }
    }

    public static void restart() throws IOException {
        if (restarting) return;
        restarting = true;
        FileUtils.touch(new File(Config.Restart.flag));
        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(
                ChatUtil.coloredString("Restarting...", TextFormatting.LIGHT_PURPLE),
                true
        );
        FMLCommonHandler.instance().getMinecraftServerInstance().initiateShutdown();
    }
}
