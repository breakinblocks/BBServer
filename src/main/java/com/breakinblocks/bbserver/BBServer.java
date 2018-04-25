package com.breakinblocks.bbserver;

import com.breakinblocks.bbserver.command.CommandRestart;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

/**
 * Main class.
 */
@Mod(modid = BBServer.MODID, name = BBServer.NAME, version = BBServer.VERSION, acceptableRemoteVersions = "*")
public class BBServer {
    public static final String MODID = "bbserver";
    public static final String NAME = "BBServer";
    public static final String VERSION = "@VERSION@";

    public static Logger log;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        BBServer.log = event.getModLog();
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        if (event.getServer().isDedicatedServer()) {
            if(Config.Restart.command) event.registerServerCommand(new CommandRestart());
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

    }
}
