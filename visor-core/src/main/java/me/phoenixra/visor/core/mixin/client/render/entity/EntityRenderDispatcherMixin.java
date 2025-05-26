package me.phoenixra.visor.core.mixin.client.render.entity;

import com.mojang.math.Axis;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;

import me.phoenixra.visor.core.client.mcmodified.render.LevelRendererModified;
import me.phoenixra.visor.core.client.render.VRRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.phoenixra.visor.core.client.ClientContext;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin implements ResourceManagerReloadListener {


    @Shadow
    public Camera camera;
    @Shadow
    private Quaternionf cameraOrientation;


    @Inject(at = @At("HEAD"), method = "cameraOrientation", cancellable = true)
    public void visor$cameraOrientation(CallbackInfoReturnable<Quaternionf> cir) {
        if (VRRenderState.getCurrentPhase().isNotVRWorld()) {
            cir.setReturnValue(cameraOrientation);
            return;
        }

        Entity entity = ((LevelRendererModified) Minecraft.getInstance().levelRenderer)
                .visor$getRenderedEntity();
        if(entity == null){
            cir.setReturnValue(this.camera.rotation());
            return;
        }
        Vec3 cameraPos = ClientContext.player
                .getPose(PoseType.RENDER)
                .getHmd().getPosition();
        if (VRRenderState.getCurrentVRDisplay() == VRDisplay.THIRD_PERSON) {
            cameraPos = ClientContext.player
                    .getPose(PoseType.RENDER)
                    .getElementForDisplay(VRDisplay.THIRD_PERSON)
                    .getPosition();
        }
        Vec3 entityToCamera = entity.position()
                .add(0.0D, entity.getBbHeight() / 2.0F, 0.0D)
                .subtract(cameraPos).normalize();
        Quaternionf orientation = new Quaternionf();
        orientation.mul(Axis.YP.rotationDegrees((float) (-Math.toDegrees(Mth.atan2(-entityToCamera.x, entityToCamera.z)))));
        orientation.mul(Axis.XP.rotationDegrees((float) (-Math.toDegrees(Math.asin(entityToCamera.y / entityToCamera.length())))));
        cir.setReturnValue(orientation);
    }


}
