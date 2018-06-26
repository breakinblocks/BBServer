package com.breakinblocks.bbserver.module;

import com.breakinblocks.bbserver.BBServer;
import com.breakinblocks.bbserver.Config;
import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;

public class Cull {
    public static final Cull instance = new Cull();
    HashSet<ResourceLocation> entities = new HashSet<>();

    private Cull() {
        for(String entityId : Config.Cull.entities) {
            entities.add(new ResourceLocation(entityId));
        }
    }

    /**
     * Prevent spawning of the entity
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        Entity entity = event.getEntity();
        ResourceLocation rl = EntityList.getKey(entity.getClass());
        // Players don't have a resource location, and other things might not as well
        if(rl == null) return;
        if(!entities.contains(rl)) return;
        event.setResult(Event.Result.DENY);

        BBServer.log.info("Denied " + event.getEntity().getName(), true);
    }

    /**
     * Prevent entity from being added to the world
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        ResourceLocation rl = EntityList.getKey(entity.getClass());
        // Players don't have a resource location, and other things might not as well
        if(rl == null) return;
        if(!entities.contains(rl)) return;
        event.setCanceled(true);

        BBServer.log.info("Culled " + event.getEntity().getName(), true);

        if(Config.Cull.notify) {
            EntityPlayer player = event.getWorld().getClosestPlayer(entity.posX, entity.posY, entity.posZ, -1, Predicates.alwaysTrue());
            if (player != null) {
                player.sendMessage(new TextComponentString("Culled " + entity.getName() + " (" + (int) (player.getDistance(entity)) + " m away)"));
            }
        }
    }

    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }
}
