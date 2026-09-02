package org.vmstudio.visor.mixin.common.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import org.vmstudio.visor.core.common.CommonUtils;

@Mixin(Item.class)
public abstract class ItemMixin {

    // buckets, boats, spawn eggs
    // and ender eye pick their target here
    @WrapOperation(method = "getPlayerPOVHitResult", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"))
    private static BlockHitResult visor$clipFromActiveHand(Level level, ClipContext context,
                                                           Operation<BlockHitResult> original,
                                                           @Local(argsOnly = true) Player player,
                                                           @Local(argsOnly = true) ClipContext.Fluid fluidMode) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(player);
        if (vrPlayer == null
                || vrPlayer.isRemote()
                || vrPlayer instanceof VRServerPlayer serverPlayer && !serverPlayer.hasPoseData()) {
            return original.call(level, context);
        }
        VRPose hand = vrPlayer.getPoseData().getHand(vrPlayer.getActiveHand());

        double reach = context.getFrom().distanceTo(context.getTo());
        Vec3 from = hand.getPositionVec3();
        Vec3 to = from.add(hand.getDirectionVec3().scale(reach));
        return original.call(level, new ClipContext(from, to, ClipContext.Block.OUTLINE, fluidMode, player));
    }
}
