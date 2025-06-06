package me.phoenixra.visor.core.mixin.client.render.entity;

import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.helpers.RenderHelper;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
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
        return RenderHelper.getControllerPosition(
                ControllerHand.MAIN
        );
    }
}
