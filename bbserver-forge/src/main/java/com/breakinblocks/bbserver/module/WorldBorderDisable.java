package com.breakinblocks.bbserver.module;

import com.breakinblocks.bbserver.BBServerConfig;
import com.google.common.collect.ImmutableSet;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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

    public static final double WORLD_BORDER_DEFAULT_SIZE = 6.0E7D;

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        LevelAccessor tempWorld = event.getWorld();
        if (!(tempWorld instanceof ServerLevel world)) return;
        ResourceLocation dimensionId = world.dimension().location();

        if (DISABLED_DIM_IDS.contains(dimensionId) && !DimensionType.OVERWORLD_LOCATION.location().equals(dimensionId)) {
            WorldBorder worldBorder = world.getWorldBorder();
            List<BorderChangeListener> originalListeners = worldBorder.listeners;
            // remove existing listeners
            originalListeners.clear();
            // set the world border to default limit
            world.getWorldBorder().setSize(WORLD_BORDER_DEFAULT_SIZE);
        }
    }

    public static void sendActualWorldBorder(ServerPlayer player) {
        // Send the actual world border OwO
        ServerLevel world = player.getLevel();
        ResourceLocation dimensionId = world.dimension().location();
        if (DISABLED_DIM_IDS.contains(dimensionId)) {
            player.connection.send(new ClientboundInitializeBorderPacket(world.getWorldBorder()));
        }
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        sendActualWorldBorder((ServerPlayer) event.getPlayer());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sendActualWorldBorder((ServerPlayer) event.getPlayer());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        sendActualWorldBorder((ServerPlayer) event.getPlayer());
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(WorldBorderDisable.class);
    }
}
