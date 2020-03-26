package com.breakinblocks.bbserver;

import static net.minecraftforge.common.config.Config.Comment;
import static net.minecraftforge.common.config.Config.RangeDouble;
import static net.minecraftforge.common.config.Config.RangeInt;

@net.minecraftforge.common.config.Config(modid = BBServer.MODID)
public class Config {
    @Comment("Restart module only works on a dedicated server.\nThe 'flag' file is written and then the server is shutdown.\nThe server run script is then responsible for detecting and deleting the 'flag' file and re-running the server instead of exiting.")
    public static Restart restart;

    @Comment("Watches for changes in files and reloads them.")
    public static Watcher watcher;

    @Comment("Removes entities from the world.\nListens in on two events:\nLivingSpawnEvent.CheckSpawn: Prevent mob spawning of the entity.\nEntityJoinWorld: Prevent existing or summoned mobs (culled).")
    public static Cull cull;

    public static class Restart {
        @Comment("Register restart command.")
        public static boolean command = false;

        @Comment("-1: disabled, 0: restart after 'delay', 1: restart at 'times' of the day.")
        public static int mode = -1;

        @Comment("Mode 0: Auto-restart period in hours.")
        @RangeDouble(min = 0, max = 24)
        public static double delay = 6;

        @Comment("Mode 1: Auto-restart hours in a day (system time zone).")
        @RangeDouble(min = 0, max = 24)
        public static double[] times = new double[]{3, 9, 15, 21};

        // 2 hours, 1 hour, 30 minutes, 10 minutes, 5 minutes, 1 minutes, 30 seconds, 10 seconds, 5, 4, 3, 2, 1
        @Comment("Notification times in seconds")
        public static int[] notifications = new int[]{2 * 60 * 60, 60 * 60, 30 * 60, 10 * 60, 5 * 60, 60, 30, 10, 5, 4, 3, 2, 1};

        @Comment("Restart flag file.")
        public static String flag = "autostart.stamp";
    }

    public static class Watcher {
        @Comment("Reload whitelist.json on change.")
        public static boolean whitelist = false;

        @Comment("Reload ops.json on change.")
        public static boolean ops = false;

        @Comment("Seconds to wait before reloading.")
        @RangeInt(min = 1)
        public static int delay = 5;
    }

    public static class Cull {
        @Comment("Entities that should be removed from the world. e.g. 'minecraft:creeper'.")
        public static String[] entities = new String[]{};

        @Comment("Notify closest player when an entity is prevented from joining the world (only when culled).")
        public static boolean notify = false;

        @Comment("Log when an entity is prevented from joining the world (only when culled).")
        public static boolean log = true;
    }
}
