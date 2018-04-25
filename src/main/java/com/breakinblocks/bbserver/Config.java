package com.breakinblocks.bbserver;

import static net.minecraftforge.common.config.Config.Comment;

@net.minecraftforge.common.config.Config(modid = BBServer.MODID)
public class Config {
    public static Restart restart;
    public static class Restart {
        @Comment("Register restart command? Only for dedicated servers.")
        public static boolean command = true;

        @Comment("Auto-restart period in hours.")
        public static double period = -1;

        @Comment("Restart flag file. File is written then the server is shutdown. Server run script is responsible for deleting the file and re-running the server instead of exiting.")
        public static String flag = "autostart.stamp";
    }
}
