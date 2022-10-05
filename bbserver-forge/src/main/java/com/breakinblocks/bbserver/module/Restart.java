package com.breakinblocks.bbserver.module;

import com.breakinblocks.bbserver.BBServerConfig;
import com.breakinblocks.bbserver.util.ChatUtil;
import com.breakinblocks.bbserver.util.MiscUtil;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Timer;

public class Restart {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Timer timer = new Timer("BBServer-Restart", true);
    public static boolean restarting = false;

    public static void createTasks() {
        // Use a consistent "now" throughout this method
        final Instant now = Instant.now();
        final Instant restartTime;

        // Initialisation
        switch (BBServerConfig.COMMON.restart.mode.get()) {
            case 0: {
                restartTime = now.plus(MiscUtil.duration(BBServerConfig.COMMON.restart.delay.get(), ChronoUnit.HOURS));
            }
            break;
            case 1: {
                // Start of the day for the system timezone
                Instant startOfDay = now.atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).toInstant();
                // If no times are specified, will restart in 24 hours
                Instant restartHourMin = now.plus(1, ChronoUnit.DAYS);
                // Get the closest next restart time
                for (double hour : BBServerConfig.COMMON.restart.times.get()) {
                    Instant restartHour = startOfDay.plus(MiscUtil.duration(hour, ChronoUnit.HOURS));
                    // If the restart time has already passed, add 24 hours to get the next restart time
                    if (restartHour.compareTo(now) < 0) {
                        restartHour = restartHour.plus(1, ChronoUnit.DAYS);
                    }
                    // If this restart time is closer to now, then take it.
                    if (restartHour.compareTo(restartHourMin) < 0) {
                        restartHourMin = restartHour;
                    }
                }
                restartTime = restartHourMin;
            }
            break;
            default:
                return;
        }

        ChatUtil.broadcastMessage(
                "Server restarts in " + DurationFormatUtils.formatDurationWords(Duration.between(now, restartTime).toMillis(), true, true),
                TextFormatting.LIGHT_PURPLE);

        // Notifications
        for (long secondsPrior : BBServerConfig.COMMON.restart.notifications.get()) {
            final Instant notificationTime = restartTime.minus(secondsPrior, ChronoUnit.SECONDS);
            if (notificationTime.compareTo(now) <= 0) continue;
            timer.schedule(MiscUtil.task(() -> {
                ChatUtil.broadcastMessage(
                        "Server restarts in " + DurationFormatUtils.formatDurationWords(Duration.between(notificationTime, restartTime).toMillis(), true, true),
                        TextFormatting.LIGHT_PURPLE);
            }), Date.from(notificationTime));
        }

        // Restart
        timer.schedule(MiscUtil.task(() -> {
            try {
                restart();
            } catch (IOException e) {
                LOGGER.error("Failed to create the restart flag file.");
                ChatUtil.broadcastMessage("Server failed to restart.", TextFormatting.DARK_RED);
            }
        }), Date.from(restartTime));
    }

    public static void restart() throws IOException {
        if (restarting) return;
        restarting = true;
        FileUtils.touch(new File(BBServerConfig.COMMON.restart.flag.get()));
        MiscUtil.sync(() -> {
            ChatUtil.broadcastMessage("Restarting...", TextFormatting.LIGHT_PURPLE);
            MiscUtil.getServer().halt(false);
        });
    }
}
