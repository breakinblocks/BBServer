package com.breakinblocks.bbserver;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class BBServerConfig {
    public static final ForgeConfigSpec commonSpec;
    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {
        public final Fixes fixes;
        public final Restart restart;
        public final Watcher watcher;
        public final Cull cull;

        public Common(Builder builder) {
            fixes = new Fixes(builder);
            restart = new Restart(builder);
            watcher = new Watcher(builder);
            cull = new Cull(builder);
        }

        public static class Fixes {
            public final ConfigValue<List<String>> noWorldBorderDimIds;

            public Fixes(Builder builder) {
                builder.comment("Fixes for various bugs")
                        .push("fixes");
                noWorldBorderDimIds = builder
                        .worldRestart()
                        .comment("Disable the world border in these dimensions e.g. `minecraft:the_end` (does not work for overworld!)")
                        .define("noWorldBorderDimIds", new ArrayList<>());
                builder.pop();
            }
        }

        public static class Restart {
            public final BooleanValue command;
            public final IntValue mode;
            public final DoubleValue delay;
            public final ConfigValue<List<Double>> times;
            public final ConfigValue<List<Integer>> notifications;
            public final ConfigValue<String> flag;

            public Restart(Builder builder) {
                builder.comment("Restart module only works on a dedicated server.\nThe 'flag' file is written and then the server is shutdown.\nThe server run script is then responsible for detecting and deleting the 'flag' file and re-running the server instead of exiting.")
                        .push("restart");
                command = builder
                        .worldRestart()
                        .comment("Register restart command.")
                        .define("command", false);
                mode = builder
                        .worldRestart()
                        .comment("-1: disabled, 0: restart after 'delay', 1: restart at 'times' of the day.")
                        .defineInRange("mode", -1, -1, 1);
                delay = builder
                        .worldRestart()
                        .comment("Mode 0: Auto-restart period in hours.")
                        .defineInRange("delay", 6d, 0, 24);
                times = builder
                        .worldRestart()
                        .comment("Mode 1: Auto-restart hours in a day (system time zone).")
                        .define("times", Lists.newArrayList(3d, 9d, 15d, 21d));
                notifications = builder
                        .worldRestart()
                        .comment("Notification times in seconds")
                        .define("notifications", Lists.newArrayList(2 * 60 * 60, 60 * 60, 30 * 60, 10 * 60, 5 * 60, 60, 30, 10, 5, 4, 3, 2, 1));
                flag = builder
                        .worldRestart()
                        .comment("Restart flag file.")
                        .define("flag", "autostart.stamp");
                builder.pop();
            }
        }

        public static class Watcher {
            public final BooleanValue whitelist;
            public final BooleanValue ops;
            public final IntValue delay;

            public Watcher(Builder builder) {
                builder.comment("Watches for changes in files and reloads them.")
                        .push("watcher");
                whitelist = builder
                        .worldRestart()
                        .comment("Reload whitelist.json on change.")
                        .define("whitelist", false);
                ops = builder
                        .worldRestart()
                        .comment("Reload ops.json on change.")
                        .define("ops", false);
                delay = builder
                        .worldRestart()
                        .comment("Seconds to wait before reloading.")
                        .defineInRange("delay", 5, 1, 60 * 60);
                builder.pop();
            }
        }

        public static class Cull {
            public final ConfigValue<List<String>> entities;
            public final BooleanValue notify;
            public final BooleanValue log;

            public Cull(Builder builder) {
                builder.comment("Removes entities from the world.\nListens in on two events:\nLivingSpawnEvent.CheckSpawn: Prevent mob spawning of the entity.\nEntityJoinWorld: Prevent existing or summoned mobs (culled).")
                        .push("cull");
                entities = builder
                        .worldRestart()
                        .comment("Entities that should be removed from the world. e.g. 'minecraft:creeper'.")
                        .define("entities", new ArrayList<>());
                notify = builder
                        .worldRestart()
                        .comment("Notify closest player when an entity is prevented from joining the world (only when culled).")
                        .define("notify", false);
                log = builder
                        .worldRestart()
                        .comment("Log when an entity is prevented from joining the world (only when culled).")
                        .define("log", true);
                builder.pop();
            }
        }
    }
}
