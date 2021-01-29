package com.breakinblocks.bbserver.module;

import com.breakinblocks.bbserver.BBServerConfig;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SWorldBorderPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IWorld;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.List;

/**
 * This class disables the world border in non-overworld dimensions (ServerWorld only)
 * It does this by:
 * - Replacing ServerWorld's IBorderListener with a dummy one (preventing it from syncing with the overworld's)
 * - Setting the World Border of the dimension to the default value of 6.0E7D
 * Additionally it attempts to send the corrected world border to clients when they need it:
 * - Logging in
 * - Respawning
 * - Dimension Change
 * But it doesn't update it when
 * - When spectators switch dimensions
 * - When the global world border is updated (people will have to relog in that case)
 */
public final class WorldBorderDisable {

    public static final ImmutableSet<ResourceLocation> DISABLED_DIM_IDS = BBServerConfig.COMMON.fixes.noWorldBorderDimIds.get().stream()
            .map(ResourceLocation::new)
            .collect(ImmutableSet.toImmutableSet());
    public static final Field field_listener;
    public static final double WORLD_BORDER_DEFAULT_SIZE = 6.0E7D;
    private static final Logger LOGGER = LogManager.getLogger();

    static {
        // net.minecraft.world.border.WorldBorder field_177758_a #listeners
        Field field = null;
        try {
            field = WorldBorder.class.getDeclaredField("listeners");
        } catch (NoSuchFieldException ignored) {
            try {
                field = WorldBorder.class.getDeclaredField("field_177758_a");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Could not find field 'borderListener' in 'ServerWorld'", e);
            }
        }
        field.setAccessible(true);
        field_listener = field;
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        IWorld tempWorld = event.getWorld();
        if (!(tempWorld instanceof ServerWorld)) return;
        ServerWorld world = (ServerWorld) tempWorld;
        ResourceLocation dimensionId = world.getDimensionKey().getLocation();

        if (DISABLED_DIM_IDS.contains(dimensionId) && !DimensionType.OVERWORLD.getLocation().equals(dimensionId)) {
            List<IBorderListener> originalListeners;
            try {
                WorldBorder worldBorder = world.getWorldBorder();
                //noinspection unchecked
                originalListeners = (List<IBorderListener>) field_listener.get(worldBorder);
                // remove existing listeners
                originalListeners.clear();
                // set the world border to default limit
                world.getWorldBorder().setTransition(WORLD_BORDER_DEFAULT_SIZE);
            } catch (IllegalAccessException e) {
                LOGGER.warn("Failed to replace borderListener for DIM " + world.getDimensionKey() + ": " + world.getProviderName(), e);
            }
        }
    }

    public static void sendActualWorldBorder(ServerPlayerEntity player) {
        // Send the actual world border OwO
        ServerWorld world = player.getServerWorld();
        ResourceLocation dimensionId = world.getDimensionKey().getLocation();
        if (DISABLED_DIM_IDS.contains(dimensionId)) {
            player.connection.sendPacket(new SWorldBorderPacket(world.getWorldBorder(), SWorldBorderPacket.Action.INITIALIZE));
        }
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        sendActualWorldBorder((ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sendActualWorldBorder((ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        sendActualWorldBorder((ServerPlayerEntity) event.getPlayer());
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(WorldBorderDisable.class);
    }
}
