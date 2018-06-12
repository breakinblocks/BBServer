package com.breakinblocks.bbserver.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ChatUtil {
    public static TextComponentString coloredString(String message, TextFormatting color) {
        TextComponentString text = new TextComponentString(message);
        text.getStyle().setColor(color);
        return text;
    }

    public static void broadcastMessage(String message, TextFormatting color, boolean isSystem) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if(!server.isCallingFromMinecraftThread()){
            server.addScheduledTask(()->{
                broadcastMessage(message, color, isSystem);
            });
            return;
        }
        server.getPlayerList().sendMessage(
                ChatUtil.coloredString(message, TextFormatting.LIGHT_PURPLE),
                isSystem
        );
    }
}
