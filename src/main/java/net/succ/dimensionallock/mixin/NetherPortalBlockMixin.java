package net.succ.dimensionallock.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.portal.DimensionTransition;
import net.succ.dimensionallock.DimensionEvents;
import net.succ.dimensionallock.LockedDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla finds or builds the exit portal before the travel event fires, so a locked
 * Nether would still get a portal generated in it. Bail out before that happens.
 */
@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
    @Inject(method = "getPortalDestination", at = @At("HEAD"), cancellable = true)
    private void dimensional_lock$blockLockedDestination(ServerLevel level, Entity entity, BlockPos pos, CallbackInfoReturnable<DimensionTransition> cir) {
        ResourceKey<Level> target = level.dimension() == Level.NETHER ? Level.OVERWORLD : Level.NETHER;
        if (LockedDimensions.get(level.getServer()).isLocked(target.location())) {
            DimensionEvents.notifyBlocked(entity);
            cir.setReturnValue(null);
        }
    }
}
