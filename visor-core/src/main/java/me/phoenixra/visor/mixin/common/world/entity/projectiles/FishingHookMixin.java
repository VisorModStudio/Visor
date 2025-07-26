package me.phoenixra.visor.mixin.common.world.entity.projectiles;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.server.player.VRServerPlayer;
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
    private VRServerPlayer visor$savedPlayer = null;
    @Unique
    private Vec3 visor$savedHandDir = null;
    @Unique
    private Vec3 visor$savedHandPos = null;

    @ModifyVariable(at = @At(value = "STORE"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", ordinal = 0)
    private float visor$vrRotationX(float xRot, Player player) {
        visor$savedPlayer = VisorAPI.server().getVrPlayer(
                (ServerPlayer) player
        );
        if (visor$savedPlayer == null || !visor$savedPlayer.isVr()) {
            return xRot;
        }
        visor$savedHandDir = visor$savedPlayer
                .getActiveHandDir();
        visor$savedHandPos = visor$savedPlayer
                .getActiveHandPos();
        return (float) Math.toDegrees(
                Math.asin(visor$savedHandDir.y / visor$savedHandDir.length())
        ) * -1;
    }

    @ModifyVariable(at = @At(value = "STORE"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", ordinal = 1)
    private float visor$vrRotationY(float yRot) {
        if (visor$savedPlayer == null || !visor$savedPlayer.isVr()) {
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
        if (visor$savedPlayer == null || !visor$savedPlayer.isVr()) {
            this.moveTo(x, y, z, yRot, xRot);
            visor$savedPlayer = null;
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
        visor$savedPlayer = null;
    }
}
