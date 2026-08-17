package org.vmstudio.visor.mixin.client.renderer.entity.item;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin extends EntityRenderer<FishingHook, FishingHookRenderState> {

    protected FishingHookRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(at = @At(value = "HEAD"), method = "render(Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
    cancellable = true)
    private void visor$noRenderOnGameScreen(CallbackInfo ci) {
        if(MC.screen != null){
            ci.cancel();
        }
    }

    @Inject(method = "getPlayerHandPos", at = @At("HEAD"), cancellable = true)
    private void visor$fishingLineStart(CallbackInfoReturnable<Vec3> cir,
                                        @Local(argsOnly = true) Player player)
    {
        if(VRRenderState.getPhase().isVanilla()
                || !this.entityRenderDispatcher.options.getCameraType().isFirstPerson()
                || player != MC.player){
            return;
        }
        var renderPose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RENDER);

        HandType handType = HandType.OFFHAND;
        if (player.getMainHandItem().getItem() instanceof FishingRodItem) {
            handType = HandType.MAIN;
        }
        Vector3f handPos = new Vector3f(
                RenderPoseHelper.getHandPosition(handType)
        );

        Vector3f handDir = renderPose
                .getGripHand(handType).getCustomVector(
                        new Vector3f(-0.05f,-0.06f,-1.0f)
                );

        float worldScale = renderPose.getWorldScale();
        Vector3f finalPos = handPos.add(
                        new Vector3f(handDir).mul(
                                0.525f * worldScale
                        )
                );

        cir.setReturnValue(new Vec3(finalPos));
    }

}
