package com.breakinblocks.bbserver.module;

import com.breakinblocks.bbserver.BBServer;
import com.breakinblocks.bbserver.Config;
import com.breakinblocks.bbserver.util.ChatUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Mod.EventBusSubscriber(modid = BBServer.MODID)
public class Restart {
    static long restartTime = -1;
    static int nextRestartMessage = 0;
    static boolean restarting = false;
    // 30 minutes, 10 minutes, 5 minutes, 1 minutes, 30 seconds, 10 seconds, 5, 4, 3, 2, 1
    static final long[] NOTIFICATIONS = new long[]{30*60, 10*60, 5*60, 60, 30, 10, 5, 4, 3, 2, 1};

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if(event.phase != TickEvent.Phase.END) return;
        if(restarting) return;
        if(Config.Restart.period > 0) {
            // Restart Initialisation
            if(restartTime == -1) {
                restartTime = System.currentTimeMillis() + (long) (Config.Restart.period * 1000 * 60 * 60);
                long remaining = (restartTime - System.currentTimeMillis()) / 1000;
                while(nextRestartMessage < NOTIFICATIONS.length && remaining <= NOTIFICATIONS[nextRestartMessage]) {
                    nextRestartMessage++;
                }
            }

            // Seconds remaining
            long remaining = (restartTime - System.currentTimeMillis()) / 1000;
            if(remaining > 0) {
                if(nextRestartMessage < NOTIFICATIONS.length && remaining <= NOTIFICATIONS[nextRestartMessage]) {
                    while(nextRestartMessage < NOTIFICATIONS.length && remaining <= NOTIFICATIONS[nextRestartMessage]) {
                        nextRestartMessage++;
                    }
                    remaining = NOTIFICATIONS[nextRestartMessage - 1];
                    String message;
                    if(remaining >= 60) {
                        message = (remaining / 60) + " min";
                    } else {
                        message = remaining + " sec";
                    }
                    message = "Restart in " + message;
                    FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(
                            ChatUtils.coloredString(message, TextFormatting.LIGHT_PURPLE),
                            true
                    );
                }
            }

            if(System.currentTimeMillis() > restartTime) {
                try {
                    FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(
                            ChatUtils.coloredString("Restarting...", TextFormatting.LIGHT_PURPLE),
                            true
                    );
                    FileUtils.touch(new File(Config.Restart.flag));
                    FMLCommonHandler.instance().getMinecraftServerInstance().initiateShutdown();
                    restarting = true;
                } catch (IOException e) {
                    BBServer.log.error("Failed to create the restart flag file.");
                    FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(
                            ChatUtils.coloredString("Server failed to restart.", TextFormatting.DARK_RED),
                            true
                    );
                }
            }
        }
    }
}
