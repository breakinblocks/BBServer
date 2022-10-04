package com.breakinblocks.bbserver;

import com.breakinblocks.bbserver.command.CommandRestart;
import com.breakinblocks.bbserver.module.BackupFreezeFix;
import com.breakinblocks.bbserver.module.Cull;
import com.breakinblocks.bbserver.module.Restart;
import com.breakinblocks.bbserver.module.Watcher;
import com.breakinblocks.bbserver.module.WorldBorderDisable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main class.
 */
@Mod(BBServer.MODID)
public class BBServer {
    public static final String MODID = "bbserver";

    public static final String FTB_BACKUPS_MODID = "ftbbackups";

    private static final Logger LOGGER = LogManager.getLogger();

    public BBServer() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BBServerConfig.commonSpec);
        // Ignore this mod being installed on either side
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (incoming, isNetwork) -> true));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        MinecraftForge.EVENT_BUS.addListener(this::serverStart);
    }

    public void serverStart(FMLServerStartedEvent event) {
        if (event.getServer().isDedicatedServer()) {
            // Restart Module
            if (BBServerConfig.COMMON.restart.command.get())
                new CommandRestart().register(event.getServer().getCommands().getDispatcher());
            if (BBServerConfig.COMMON.restart.mode.get() >= 0) Restart.createTasks();
            // Watcher Module
            Watcher.setup();
        }
    }

    public void init(FMLCommonSetupEvent event) {
        Cull.init();

        if (BBServerConfig.COMMON.fixes.backupFreeze.get() && ModList.get().isLoaded(FTB_BACKUPS_MODID))
            BackupFreezeFix.init();

        if (!BBServerConfig.COMMON.fixes.noWorldBorderDimIds.get().isEmpty())
            WorldBorderDisable.init();
    }
}
