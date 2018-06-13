package com.breakinblocks.bbserver.util;

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
        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(
                ChatUtil.coloredString(message, TextFormatting.LIGHT_PURPLE),
                isSystem
        );
    }
}
