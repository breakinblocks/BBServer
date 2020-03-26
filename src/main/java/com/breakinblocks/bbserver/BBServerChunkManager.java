package com.breakinblocks.bbserver;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

import java.util.HashMap;
import java.util.List;

public final class BBServerChunkManager {
    private static final String TAG_MODULE = "module";
    private static final HashMap<String, TicketHandler> handlers = new HashMap<>();

    public static void ticketsLoaded(List<Ticket> tickets, World world) {
        tickets.forEach(ticket -> {
            String module = ticket.getModData().getString(TAG_MODULE);
            TicketHandler handler = handlers.get(module);
            if (handler != null) {
                handler.loadTicket(ticket, world);
            } else {
                BBServer.log.warn("No ticket handler for module '" + module + "'. Ticket discarded.");
            }
        });
    }

    public static void registerHandler(String module, TicketHandler handler) {
        if (handlers.containsKey(module))
            throw new RuntimeException("Module " + module + " tried to register two handlers");
        handlers.put(module, handler);
    }

    public static Ticket requestTicket(String module, World world, Type type) {
        Ticket ticket = ForgeChunkManager.requestTicket(BBServer.instance, world, type);
        ticket.getModData().setString(TAG_MODULE, module);
        return ticket;
    }

    public static void init() {
        ForgeChunkManager.setForcedChunkLoadingCallback(BBServer.instance, BBServerChunkManager::ticketsLoaded);
    }

    /**
     * Re-register tickets if needed
     */
    @FunctionalInterface
    public interface TicketHandler {
        void loadTicket(Ticket ticket, World world);
    }
}
