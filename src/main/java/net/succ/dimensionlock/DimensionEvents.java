package net.succ.dimensionlock;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Dimensionlock.MODID)
public class DimensionEvents {

    // Covers portals, /tp, /execute in ... and most modded teleporters
    @SubscribeEvent
    public static void onTravel(EntityTravelToDimensionEvent event) {
        var server = event.getEntity().getServer();
        if (server == null) return;
        if (!LockedDimensions.get(server).isLocked(event.getDimension().location())) return;

        event.setCanceled(true);
        notifyBlocked(event.getEntity());
    }

    public static void notifyBlocked(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.literal("That dimension is locked.").withStyle(ChatFormatting.RED), true);
        }
    }

    // Player logged out in a dimension that has since been locked
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) evictIfLocked(player);
    }

    // Safety net for teleports that bypass the travel event
    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) evictIfLocked(player);
    }

    private static void evictIfLocked(ServerPlayer player) {
        if (!Dimensionlock.canBeLocked(player.level().dimension())) return;
        if (LockedDimensions.get(player.server).isLocked(player.level().dimension().location())) {
            sendToOverworld(player);
        }
    }

    public static void sendToOverworld(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.displayClientMessage(Component.literal("That dimension is locked, you've been sent to spawn.").withStyle(ChatFormatting.RED), false);
    }
}
