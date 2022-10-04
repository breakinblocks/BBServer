package com.breakinblocks.bbserver.module;

import com.breakinblocks.bbserver.BBServer;
import com.breakinblocks.bbserver.Config;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketWorldBorder;
import net.minecraft.world.World;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * This class disables the world border in non-overworld dimensions (WorldServerMulti only)
 * It does this by:
 * - Replacing WorldServerMulti's IBorderListener with a dummy one (preventing it from syncing with the overworld's)
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

    public static final ImmutableSet<Integer> DISABLED_DIM_IDS = new ImmutableSet.Builder<Integer>()
            .addAll(Arrays.stream(Config.Fixes.noWorldBorderDimIds).iterator())
            .build();

    public static final double WORLD_BORDER_DEFAULT_SIZE = 6.0E7D;

    public static final Field field_borderListener;

    static {
        try {
            field_borderListener = WorldServerMulti.class.getDeclaredField("borderListener");
            field_borderListener.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not find field 'borderListener' in 'WorldServerMulti'", e);
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        int dimensionId = world.provider.getDimension();
        if (DISABLED_DIM_IDS.contains(dimensionId) && world instanceof WorldServerMulti) {
            WorldServerMulti worldServerMulti = (WorldServerMulti) world;
            // Replace the border listener with a dummy one
            try {

                WorldBorder worldBorder = worldServerMulti.delegate.getWorldBorder();
                IBorderListener originalListener = (IBorderListener) field_borderListener.get(worldServerMulti);
                // remove originalListener
                worldBorder.removeListener(originalListener);
                IBorderListener dummyListener = new DummyBorderListener();
                // add dummyListener
                worldBorder.addListener(dummyListener);
                field_borderListener.set(worldServerMulti, dummyListener);
            } catch (IllegalAccessException e) {
                BBServer.log.warn("Failed to replace borderListener for DIM" + dimensionId + ": " + world.provider, e);
            }
            // set the world border to default limit
            worldServerMulti.getWorldBorder().setTransition(WORLD_BORDER_DEFAULT_SIZE);
        }
    }

    public static void sendActualWorldBorder(EntityPlayerMP player) {
        // Send the actual world border OwO
        World world = player.world;
        int dimensionId = world.provider.getDimension();
        if (DISABLED_DIM_IDS.contains(dimensionId) && world instanceof WorldServerMulti) {
            player.connection.sendPacket(new SPacketWorldBorder(world.getWorldBorder(), SPacketWorldBorder.Action.INITIALIZE));
        }
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        sendActualWorldBorder((EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sendActualWorldBorder((EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        sendActualWorldBorder((EntityPlayerMP) event.player);
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(WorldBorderDisable.class);
    }

    public static class DummyBorderListener implements IBorderListener {

        @Override
        public void onSizeChanged(WorldBorder border, double newSize) {

        }

        @Override
        public void onTransitionStarted(WorldBorder border, double oldSize, double newSize, long time) {

        }

        @Override
        public void onCenterChanged(WorldBorder border, double x, double z) {

        }

        @Override
        public void onWarningTimeChanged(WorldBorder border, int newTime) {

        }

        @Override
        public void onWarningDistanceChanged(WorldBorder border, int newDistance) {

        }

        @Override
        public void onDamageAmountChanged(WorldBorder border, double newAmount) {

        }

        @Override
        public void onDamageBufferChanged(WorldBorder border, double newSize) {

        }
    }
}
