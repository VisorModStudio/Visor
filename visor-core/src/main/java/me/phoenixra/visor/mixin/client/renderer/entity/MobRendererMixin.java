package me.phoenixra.visor.mixin.client.renderer.entity;

import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(MobRenderer.class)
public class MobRendererMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getRopeHoldPosition(F)Lnet/minecraft/world/phys/Vec3;"), method = "renderLeash")
    public Vec3 visor$vrRenderLeash(Entity instance, float partialTick) {
        if (VRRenderState.getCurrentPhase().isNotVRWorld()) {
            return instance.getRopeHoldPosition(partialTick);
        }
        return new Vec3((Vector3f) RenderPoseHelper.getControllerPosition(
                ControllerHand.MAIN
        ));
    }
}
