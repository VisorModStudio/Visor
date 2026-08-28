package org.vmstudio.visor.mixin.common.world.entity.projectiles;

import org.vmstudio.visor.core.common.CommonUtils;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Entity {

    protected FishingHookMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private VRServerPlayer visor$vrPlayer = null;
    @Unique
    private Vec3 visor$savedHandDir = null;
    @Unique
    private Vec3 visor$savedHandPos = null;

    @ModifyVariable(at = @At(value = "STORE"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", ordinal = 0)
    private float visor$vrRotationX(float xRot, Player player) {
        visor$vrPlayer = null;
        // some mods mess with this
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return xRot;
        }
        visor$vrPlayer = VisorAPI.server().getVRPlayer(serverPlayer);
        if (visor$vrPlayer == null) {
            return xRot;
        }
        var activeHand = visor$vrPlayer.getPoseData().getActiveHand();

        visor$savedHandPos = activeHand.getPositionVec3();
        visor$savedHandDir = activeHand.getDirectionVec3();

        return CommonUtils.pitchFromDirection(visor$savedHandDir);
    }

    @ModifyVariable(at = @At(value = "STORE"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", ordinal = 1)
    private float visor$vrRotationY(float yRot) {
        if (visor$vrPlayer == null) {
            return yRot;
        }
        return CommonUtils.yawFromDirection(visor$savedHandDir);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;moveTo(DDDFF)V"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V")
    private void visor$vrMoveTo(FishingHook instance, double x, double y, double z, float yRot, float xRot) {
        if (visor$vrPlayer == null) {
            instance.moveTo(x, y, z, yRot, xRot);
            return;
        }

        final double rodTipOffset = 0.6D;
        instance.moveTo(
                visor$savedHandPos.x + visor$savedHandDir.x * rodTipOffset,
                visor$savedHandPos.y + visor$savedHandDir.y * rodTipOffset,
                visor$savedHandPos.z + visor$savedHandDir.z * rodTipOffset,
                yRot, xRot
        );
        visor$savedHandDir = null;
        visor$savedHandPos = null;
        visor$vrPlayer = null;
    }
}
