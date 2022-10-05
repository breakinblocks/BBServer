package com.breakinblocks.bbserver.util;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.TimerTask;

public class MiscUtil {
    public static MinecraftServer getServer() {
        return LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
    }

    public static TimerTask task(final Runnable r) {
        return new TimerTask() {
            @Override
            public void run() {
                r.run();
            }
        };
    }

    public static Duration duration(double amount, TemporalUnit unit) {
        return Duration.ofMillis((long) (amount * unit.getDuration().toMillis()));
    }

    /**
     * Run on the main server thread
     */
    public static void sync(Runnable r) {
        getServer().submit(r);
    }
}
