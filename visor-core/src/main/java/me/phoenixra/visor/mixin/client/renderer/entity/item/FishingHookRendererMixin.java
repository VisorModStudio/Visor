package me.phoenixra.visor.mixin.client.renderer.entity.item;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin extends EntityRenderer<FishingHook> {

    protected FishingHookRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Unique
    private Vec3 visor$savedHandPos;

    @Inject(at = @At(value = "HEAD"), method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
    cancellable = true)
    private void visor$noRenderOnGameScreen(FishingHook fishingHook,
                                           float f, float g,
                                           PoseStack poseStack,
                                           MultiBufferSource multiBufferSource,
                                           int i,
                                           CallbackInfo ci
    ){
        if(MC.screen != null){
            ci.cancel();
        }
    }

    @ModifyVariable(at = @At(value = "LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 25)
    private double visor$fishingLineStartX(double value, FishingHook fishingHook) {
        if(VRRenderState.getCurrentPhase().isVanilla()
                || !this.entityRenderDispatcher.options.getCameraType().isFirstPerson()
                || fishingHook.getPlayerOwner() != MC.player){
            return value;
        }
        var renderPose = ClientContext.player
                .getPoseData(PoseDataType.RENDER);

        ControllerHand handType = ControllerHand.OFFHAND;
        if (fishingHook.getPlayerOwner().getMainHandItem().getItem() instanceof FishingRodItem) {
            handType = ControllerHand.MAIN;
        }
        Vector3f handPos = new Vector3f(
                RenderPoseHelper.getControllerPosition(handType)
        );

        Vector3f handDir = renderPose
                .getHand(handType).getCustomVector(
                        new Vector3f(-0.05f,-0.06f,-1.0f)
                );

        float worldScale = renderPose.getWorldScale();
        Vector3f finalPos = handPos.add(
                        new Vector3f(handDir).mul(
                                0.525f * worldScale
                        )
                );

        visor$savedHandPos = new Vec3(finalPos);

        return visor$savedHandPos.x;
    }

    @ModifyVariable(at = @At(value = "LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 27)
    private double visor$fishingLineStartY(double value, FishingHook fishingHook) {
        if(VRRenderState.getCurrentPhase().isVanilla()
                || !this.entityRenderDispatcher.options.getCameraType().isFirstPerson()
                || fishingHook.getPlayerOwner() != MC.player){
            return value;
        }

        return visor$savedHandPos.y;
    }

    @ModifyVariable(at = @At(value = "LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 29)
    private double visor$fishingLineStartZ(double value, FishingHook fishingHook) {
        if(VRRenderState.getCurrentPhase().isVanilla()
                || !this.entityRenderDispatcher.options.getCameraType().isFirstPerson()
                || fishingHook.getPlayerOwner() != MC.player){
            return value;
        }

        return visor$savedHandPos.z;
    }

}
