package com.breakinblocks.bbserver.command;

import com.breakinblocks.bbserver.module.Restart;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

import java.io.IOException;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class CommandRestart {
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("restart")
                .requires(source -> source.hasPermissionLevel(4))
                .executes(context -> {
                    if (Restart.restarting) {
                        context.getSource().sendErrorMessage(new StringTextComponent("Server is already restarting!"));
                        return 0;
                    }

                    try {
                        Restart.restart();
                    } catch (IOException e) {
                        context.getSource().sendErrorMessage(new StringTextComponent("Failed to create the restart flag file."));
                        return 0;
                    }
                    return SINGLE_SUCCESS;
                }));
    }
}
