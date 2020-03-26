package com.breakinblocks.bbserver.module;

import com.breakinblocks.bbserver.BBServer;
import com.breakinblocks.bbserver.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;

public class Cull {
    private static final HashSet<ResourceLocation> entities = new HashSet<>();

    /**
     * Prevent spawning of the entity
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        ResourceLocation rl = EntityList.getKey(event.getEntity().getClass());
        // Players don't have a resource location, and other things might not as well
        if(rl != null && entities.contains(rl))
            event.setResult(Event.Result.DENY);
    }

    /**
     * Prevent entity from being added to the world
     * This removes entities that exist from world gen or previously saved chunks
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        ResourceLocation rl = EntityList.getKey(entity.getClass());
        // Players don't have a resource location, and other things might not as well
        if(rl != null && entities.contains(rl)) {
            event.setCanceled(true);
            logEntityCull(entity);
        }
    }

    private static void logEntityCull(Entity entity) {
        EntityPlayer player = entity.world.getClosestPlayer(entity.posX, entity.posY, entity.posZ, -1, p -> true);
        if(player == null) {
            if(Config.Cull.log)
                BBServer.log.info("Culled " + entity.getName(), true);
        } else {
            int distance = (int)(player.getDistance(entity));
            if(Config.Cull.log)
                BBServer.log.info(String.format("Culled %s (%d m away from %s)", entity.getName(), distance, player.getName()));
            if(Config.Cull.notify)
                player.sendMessage(new TextComponentString(String.format("Culled %s (%d m away)", entity.getName(), distance)));
        }
    }

    public static void init() {
        if (Config.Cull.entities.length <= 0)
            return;
        MinecraftForge.EVENT_BUS.register(Cull.class);
        for(String entityId : Config.Cull.entities) {
            entities.add(new ResourceLocation(entityId));
        }
    }
}
