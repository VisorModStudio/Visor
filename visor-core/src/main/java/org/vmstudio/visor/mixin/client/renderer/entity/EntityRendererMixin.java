package org.vmstudio.visor.mixin.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.extensions.client.entity.EntityRenderDispatcherExtension;
import org.vmstudio.visor.extensions.client.entity.EntityRenderStateExtension;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    /**
     * 1.21.2: renderNameTag is handed a render state instead of the entity, so the VR player
     * both name tag hooks below need is resolved here and parked on the state.
     * <p>
     * Set unconditionally - render states are pooled per renderer, so skipping the write on a
     * non-VR entity would leave the previous entity's player behind.
     */
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void visor$extractVRPlayer(Entity entity, EntityRenderState renderState,
                                       float partialTick, CallbackInfo ci) {
        boolean trackable = entity instanceof Player
                && ClientContext.localPlayer != null;
        ((EntityRenderStateExtension) renderState).visor$setVRPlayer(
                trackable ? VRClientPlayers.getPlayer(entity) : null
        );
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;cameraOrientation()Lorg/joml/Quaternionf;"), method = "renderNameTag")
    public Quaternionf visor$vrNameTagCameraOrient(EntityRenderDispatcher instance, EntityRenderState renderState) {
        float heightScale = 1.0f;
        VRClientPlayer vrPlayer = ((EntityRenderStateExtension) renderState).visor$getVRPlayer();
        if (vrPlayer != null) {
            heightScale = vrPlayer.getFullHeightScale();
        }
        return ((EntityRenderDispatcherExtension) this.entityRenderDispatcher)
                .visor$getCameraOrientationOffset(heightScale, 0.5f * heightScale);
    }

    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
    private void visor$hideSpectatedVRNameTag(EntityRenderState renderState, Component displayName,
                                                PoseStack poseStack, MultiBufferSource buffer,
                                                int packedLight, CallbackInfo ci) {
        VRClientPlayer vrPlayer = ((EntityRenderStateExtension) renderState).visor$getVRPlayer();
        if (vrPlayer != null
                && VRRenderState.isSpectatedVRView(vrPlayer.getMcPlayer())) {
            ci.cancel();
        }
    }

    /**
     * 1.21.1: leash rendering moved from MobRenderer to EntityRenderer
     * (Leashable rework), so the hand-held leash end is redirected here
     * <p>
     * 1.21.2: renderLeash only sees the LeashState snapshot - the rope hold position is
     * resolved during extractRenderState now, so the redirect moved with it
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getRopeHoldPosition(F)Lnet/minecraft/world/phys/Vec3;"), method = "extractRenderState")
    public Vec3 visor$vrRenderLeash(Entity instance, float partialTick) {
        if (VRRenderState.getPhase().isNotVRWorld()) {
            return instance.getRopeHoldPosition(partialTick);
        }

        if (!(instance instanceof Player player)) {
            return instance.getRopeHoldPosition(partialTick);
        }

        var vrPlayer = VRClientPlayers.getPlayer(player);
        if (vrPlayer == null) {
            return instance.getRopeHoldPosition(partialTick);
        }

        return new Vec3(
                new Vector3f(
                        vrPlayer.getPoseData(PlayerPoseType.RENDER)
                                .getHand(HandType.MAIN)
                                .getPosition()
                )
        );
    }
}
