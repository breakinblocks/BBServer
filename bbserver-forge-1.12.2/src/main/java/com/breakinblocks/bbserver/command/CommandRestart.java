package com.breakinblocks.bbserver.command;

import com.breakinblocks.bbserver.module.Restart;
import com.breakinblocks.bbserver.util.ChatUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;

public class CommandRestart extends CommandBase {
    @Override
    public String getName() {
        return "restart";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (Restart.restarting) {
            sender.sendMessage(ChatUtil.coloredString("Server is already restarting!", TextFormatting.DARK_RED));
            return;
        }
        try {
            Restart.restart();
        } catch (IOException e) {
            sender.sendMessage(ChatUtil.coloredString("Failed to create the restart flag file.", TextFormatting.DARK_RED));
        }
    }
}
