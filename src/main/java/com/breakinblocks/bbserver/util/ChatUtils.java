package com.breakinblocks.bbserver.util;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class ChatUtils {
    public static TextComponentString coloredString(String message, TextFormatting color) {
        TextComponentString text = new TextComponentString(message);
        text.getStyle().setColor(color);
        return text;
    }
}
