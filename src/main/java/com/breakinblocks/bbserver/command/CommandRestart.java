package com.breakinblocks.bbserver.command;

import com.breakinblocks.bbserver.Config;
import com.breakinblocks.bbserver.util.ChatUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
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
        try {
            FileUtils.touch(new File(Config.Restart.flag));
            FMLCommonHandler.instance().getMinecraftServerInstance().initiateShutdown();
        } catch (IOException e) {
            sender.sendMessage(ChatUtils.coloredString("Failed to create the restart flag file.", TextFormatting.DARK_RED));
        }
    }
}
