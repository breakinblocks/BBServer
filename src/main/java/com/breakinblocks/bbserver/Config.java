package com.breakinblocks.bbserver;

import static net.minecraftforge.common.config.Config.Comment;
import static net.minecraftforge.common.config.Config.RangeDouble;

@net.minecraftforge.common.config.Config(modid = BBServer.MODID)
public class Config {
    public static Restart restart;

    public static class Restart {
        @Comment("Register restart command? Only for dedicated servers.")
        public static boolean command = true;

        @Comment("-1: disabled, 0: restart after 'delay', 1: restart at 'times' of the day.")
        public static int mode = -1;

        @Comment("Auto-restart period in hours.")
        @RangeDouble(min = 0, max = 24)
        public static double delay = 6;

        @Comment("Auto-restart hours in a day (system time zone).")
        @RangeDouble(min = 0, max = 24)
        public static double[] times = new double[]{3, 9, 15, 21};

        @Comment("Restart flag file. File is written then the server is shutdown. Server run script is responsible for deleting the file and re-running the server instead of exiting.")
        public static String flag = "autostart.stamp";
    }
}
