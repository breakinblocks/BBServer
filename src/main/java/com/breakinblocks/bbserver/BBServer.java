package com.breakinblocks.bbserver;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = BBServer.MODID, name = BBServer.NAME, version = BBServer.VERSION, acceptableRemoteVersions = "*")
public class BBServer
{
    public static final String MODID = "bbserver";
    public static final String NAME = "BBServer";
    public static final String VERSION = "@VERSION@";

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        logger.info("Init Complete");
    }
}
