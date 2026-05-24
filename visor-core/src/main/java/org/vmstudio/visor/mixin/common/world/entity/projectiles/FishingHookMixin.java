package org.vmstudio.visor.mixin.common.world.entity.projectiles;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
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
        visor$vrPlayer = VisorAPI.server().getVRPlayer(
                (ServerPlayer) player
        );
        if (visor$vrPlayer == null) {
            return xRot;
        }
        var activeHand = visor$vrPlayer.getPoseData().getActiveHand();

        visor$savedHandPos = activeHand.getPositionVec3();
        visor$savedHandDir = activeHand.getDirectionVec3();

        return (float) Math.toDegrees(
                Math.asin(visor$savedHandDir.y / visor$savedHandDir.length())
        ) * -1;
    }

    @ModifyVariable(at = @At(value = "STORE"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", ordinal = 1)
    private float visor$vrRotationY(float yRot) {
        if (visor$vrPlayer == null) {
            return yRot;
        }
        return (float) Math.toDegrees(
                Mth.atan2(
                        -visor$savedHandDir.x,
                        visor$savedHandDir.z
                )
        );
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;moveTo(DDDFF)V"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V")
    private void visor$vrMoveTo(FishingHook instance, double x, double y, double z, float yRot, float xRot) {
        if (visor$vrPlayer == null) {
            this.moveTo(x, y, z, yRot, xRot);
            visor$vrPlayer = null;
            return;
        }

        instance.moveTo(
                visor$savedHandPos.x + visor$savedHandDir.x
                        * (double) 0.6F,
                visor$savedHandPos.y + visor$savedHandDir.y
                        * (double) 0.6F,
                visor$savedHandPos.z + visor$savedHandDir.z
                        * (double) 0.6F,
                yRot, xRot
        );
        visor$savedHandDir = null;
        visor$savedHandPos = null;
        visor$vrPlayer = null;
    }
}
