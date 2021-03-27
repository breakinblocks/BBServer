package com.breakinblocks.bbserver.util;

import net.minecraft.command.ICommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

public class ChatUtil {
    public static StringTextComponent coloredString(String message, TextFormatting color) {
        StringTextComponent text = new StringTextComponent(message);
        text.getStyle().applyFormatting(color);
        return text;
    }

    public static void broadcastMessage(String message, TextFormatting color) {
        ((MinecraftServer) LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER)).getPlayerList().func_232641_a_(
                coloredString(message, color),
                ChatType.CHAT,
                Util.DUMMY_UUID
        );
    }

    public static void broadcastMessage(String message) {
        ((MinecraftServer) LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER)).getPlayerList().func_232641_a_(
                new StringTextComponent(message),
                ChatType.CHAT,
                Util.DUMMY_UUID
        );
    }

    public static void sendMessage(ICommandSource target, String message, TextFormatting color) {
        target.sendMessage(coloredString(message, color), Util.DUMMY_UUID);
    }

    public static void sendMessage(ICommandSource target, String message) {
        target.sendMessage(new StringTextComponent(message), Util.DUMMY_UUID);
    }
}
