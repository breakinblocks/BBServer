package com.breakinblocks.bbserver.util;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;

public class ChatUtil {
    public static TextComponent coloredString(String message, ChatFormatting color) {
        TextComponent text = new TextComponent(message);
        text.getStyle().applyFormat(color);
        return text;
    }

    public static void broadcastMessage(String message, ChatFormatting color) {
        ((MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER)).getPlayerList().broadcastMessage(
                coloredString(message, color),
                ChatType.CHAT,
                Util.NIL_UUID
        );
    }

    public static void broadcastMessage(String message) {
        ((MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER)).getPlayerList().broadcastMessage(
                new TextComponent(message),
                ChatType.CHAT,
                Util.NIL_UUID
        );
    }

    public static void sendMessage(CommandSource target, String message, ChatFormatting color) {
        target.sendMessage(coloredString(message, color), Util.NIL_UUID);
    }

    public static void sendMessage(CommandSource target, String message) {
        target.sendMessage(new TextComponent(message), Util.NIL_UUID);
    }
}
