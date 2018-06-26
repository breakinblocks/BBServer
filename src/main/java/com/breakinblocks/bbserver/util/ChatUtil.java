package com.breakinblocks.bbserver.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ChatUtil {
    public static TextComponentString coloredString(String message, TextFormatting color) {
        TextComponentString text = new TextComponentString(message);
        text.getStyle().setColor(color);
        return text;
    }

    public static void broadcastMessage(String message, TextFormatting color) {
        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(
                coloredString(message, color)
        );
    }

    public static void broadcastMessage(String message) {
        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(
                new TextComponentString(message)
        );
    }

    public static void sendMessage(ICommandSender target, String message, TextFormatting color) {
        target.sendMessage(coloredString(message, color));
    }

    public static void sendMessage(ICommandSender target, String message) {
        target.sendMessage(new TextComponentString(message));
    }
}
