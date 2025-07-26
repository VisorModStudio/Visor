package me.phoenixra.visor.mixin.client.renderer.entity;

import com.mojang.math.Axis;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.modified.client.entity.EntityRenderDispatcherVRModified;
import me.phoenixra.visor.modified.client.render.LevelRendererModified;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.player.VRPlayerRendererArms;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;
import java.util.Map;


@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin implements ResourceManagerReloadListener, EntityRenderDispatcherVRModified {


    @Shadow
    public Camera camera;
    @Shadow
    private Quaternionf cameraOrientation;

    @Unique
    private VRPlayerRendererArms visor$armRenderer;
    @Unique
    public final Map<String, VRPlayerRendererArms> visor$armSkinMap = new HashMap<>();


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
        var cameraPos = ClientContext.player.getPoseData(PoseDataType.RENDER).getHmd().getPosition();
        if (VRRenderState.getCurrentVRDisplay() == VRDisplay.THIRD_PERSON) {
            cameraPos = ClientContext.player.getPoseData(PoseDataType.RENDER)
                    .getElementForDisplay(VRDisplay.THIRD_PERSON)
                    .getPosition();
        }
        Vec3 entityToCamera = entity.position().add(0.0D, entity.getBbHeight() / 2.0F, 0.0D)
                .subtract(new Vec3((Vector3f) cameraPos)).normalize();
        Quaternionf orientation = new Quaternionf();
        orientation.mul(Axis.YP.rotationDegrees((float) (-Math.toDegrees(Mth.atan2(-entityToCamera.x, entityToCamera.z)))));
        orientation.mul(Axis.XP.rotationDegrees((float) (-Math.toDegrees(Math.asin(entityToCamera.y / entityToCamera.length())))));
        cir.setReturnValue(orientation);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createPlayerRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;", shift = At.Shift.AFTER),
            method = "onResourceManagerReload(Lnet/minecraft/server/packs/resources/ResourceManager;)V", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void visor$reload(ResourceManager resourceManager, CallbackInfo ci, EntityRendererProvider.Context context) {
        this.visor$armRenderer = new VRPlayerRendererArms(
                context, false
        );
        this.visor$armSkinMap.put(
                "default",
                this.visor$armRenderer
        );
        this.visor$armSkinMap.put(
                "slim",
                new VRPlayerRendererArms(context, true)
        );
    }

    @Override
    @Unique
    public Quaternionf visor$getCameraOrientationOffset(float offset) {
        if (VRRenderState.getCurrentPhase().isNotVRWorld()) {
            return cameraOrientation;
        }
        Entity entity = ((LevelRendererModified) Minecraft.getInstance().levelRenderer)
                .visor$getRenderedEntity();
        if (entity == null) {
            return this.camera.rotation();
        }
        var cameraPos = ClientContext.player
                .getPoseData(PoseDataType.RENDER)
                .getHmd()
                .getPosition();
        if (VRRenderState.getCurrentVRDisplay() == VRDisplay.THIRD_PERSON) {
            cameraPos = ClientContext.player.getPoseData(PoseDataType.RENDER)
                    .getElementForDisplay(VRDisplay.THIRD_PERSON)
                    .getPosition();
        }
        Vec3 entityToCameraDirection = entity.position().add(
                0.0D,
                entity.getBbHeight() + offset,
                0.0D
        ).subtract(new Vec3((Vector3f) cameraPos)).normalize();
        Quaternionf orient = new Quaternionf();
        orient.mul(Axis.YP.rotationDegrees(
                (float) (-Math.toDegrees(
                        Mth.atan2(
                                -entityToCameraDirection.x,
                                entityToCameraDirection.z
                        )
                )
                ))
        );
        orient.mul(Axis.XP.rotationDegrees(
                (float) (-Math.toDegrees(
                        Math.asin(
                                entityToCameraDirection.y / entityToCameraDirection.length()
                        )
                )
                ))
        );
        return orient;
    }

    public Map<String, VRPlayerRendererArms> visor$getArmSkinMap() {
        return visor$armSkinMap;
    }
}
