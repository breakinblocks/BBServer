package com.breakinblocks.bbserver.module;

import com.breakinblocks.bbserver.BBServerConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;

public class Cull {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final HashSet<ResourceLocation> entities = new HashSet<>();

    /**
     * Prevent spawning of the entity
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        ResourceLocation rl = event.getEntity().getType().getRegistryName();
        // Players don't have a resource location, and other things might not as well
        if (rl != null && entities.contains(rl))
            event.setResult(Event.Result.DENY);
    }

    /**
     * Prevent entity from being added to the world
     * This removes entities that exist from world gen or previously saved chunks
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        ResourceLocation rl = entity.getType().getRegistryName();
        // Players don't have a resource location, and other things might not as well
        if (rl != null && entities.contains(rl)) {
            event.setCanceled(true);
            logEntityCull(entity);
        }
    }

    private static void logEntityCull(Entity entity) {
        PlayerEntity player = entity.level.getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), -1, p -> true);
        if (player == null) {
            if (BBServerConfig.COMMON.cull.log.get())
                LOGGER.info("Culled " + entity.getName());
        } else {
            int distance = (int) (player.distanceTo(entity));
            if (BBServerConfig.COMMON.cull.log.get())
                LOGGER.info(String.format("Culled %s (%d m away from %s)", entity.getName(), distance, player.getName()));
            if (BBServerConfig.COMMON.cull.notify.get())
                player.sendMessage(new StringTextComponent(String.format("Culled %s (%d m away)", entity.getName(), distance)), Util.NIL_UUID);
        }
    }

    public static void init() {
        if (BBServerConfig.COMMON.cull.entities.get().isEmpty())
            return;
        MinecraftForge.EVENT_BUS.register(Cull.class);
        for (String entityId : BBServerConfig.COMMON.cull.entities.get()) {
            entities.add(new ResourceLocation(entityId));
        }
    }
}
