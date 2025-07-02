package me.phoenixra.visor.core.mixin.client.world;

import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin {

    @Shadow
    private @Nullable LivingEntity attachedToEntity;

    @Unique
    private Vec3 visor$handPos = null;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), index = 1, method = "tick")
    private double visor$modifyX(double x) {
        if(VisorState.getState().isNotActive()){
            return x;
        }
        if(attachedToEntity != MC.player){
            return x;
        }
        if(!(attachedToEntity instanceof LocalPlayer localPlayer)){
            return x;
        }
        ControllerHand handWithFirework = !localPlayer.getOffhandItem().is(Items.FIREWORK_ROCKET)
                && localPlayer.getMainHandItem().is(Items.FIREWORK_ROCKET)
                ? ControllerHand.MAIN
                : ControllerHand.OFFHAND;

        var handElement = ClientContext.player
                .getPoseData(PoseDataType.RENDER).getHand(
                        handWithFirework
                );
        Vector3f position = new Vector3f(handElement.getPosition());
        Vector3f dir = new Vector3f(handElement.getDirection());
        visor$handPos = new Vec3(
                position
                .add(dir.mul(0.3f))
        );
        return visor$handPos.x;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), index = 2, method = "tick")
    private double visor$modifyYPosOnLaunch(double y) {
        if (visor$handPos != null) {
            return visor$handPos.y;
        }
        return y;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), index = 3, method = "tick")
    private double visor$modifyZPosOnLaunch(double z) {
        if (visor$handPos != null) {
            z = visor$handPos.z;
            visor$handPos = null;
        }
        return z;
    }
}
