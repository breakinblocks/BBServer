package com.breakinblocks.bbserver;

import com.breakinblocks.bbserver.command.CommandRestart;
import com.breakinblocks.bbserver.module.Cull;
import com.breakinblocks.bbserver.module.Restart;
import com.breakinblocks.bbserver.module.Watcher;
import com.breakinblocks.bbserver.module.WorldBorderDisable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main class.
 */
@Mod(BBServer.MODID)
public class BBServer {
    public static final String MODID = "bbserver";

    private static final Logger LOGGER = LogManager.getLogger();

    public BBServer() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BBServerConfig.commonSpec);
        // Ignore this mod being installed on either side
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (incoming, isNetwork) -> true));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        MinecraftForge.EVENT_BUS.addListener(this::serverStart);
    }

    public void serverStart(ServerStartedEvent event) {
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

        if (!BBServerConfig.COMMON.fixes.noWorldBorderDimIds.get().isEmpty())
            WorldBorderDisable.init();
    }
}
