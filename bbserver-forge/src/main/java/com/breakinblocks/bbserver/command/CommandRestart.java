package com.breakinblocks.bbserver.command;

import com.breakinblocks.bbserver.module.Restart;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

import java.io.IOException;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class CommandRestart {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("restart")
                .requires(source -> source.hasPermission(4))
                .executes(context -> {
                    if (Restart.restarting) {
                        context.getSource().sendFailure(new TextComponent("Server is already restarting!"));
                        return 0;
                    }

                    try {
                        Restart.restart();
                    } catch (IOException e) {
                        context.getSource().sendFailure(new TextComponent("Failed to create the restart flag file."));
                        return 0;
                    }
                    return SINGLE_SUCCESS;
                }));
    }
}
