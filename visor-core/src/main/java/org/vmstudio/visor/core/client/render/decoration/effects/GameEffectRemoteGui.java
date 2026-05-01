package org.vmstudio.visor.core.client.render.decoration.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import org.vmstudio.visor.api.client.render.decoration.effects.VRGameEffect;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.helpers.RenderHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import me.phoenixra.atumvr.api.misc.color.AtumColorImmutable;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;

@RegisterVRGameEffect
public class GameEffectRemoteGui extends VRGameEffect {
    public static final String ID = "remote_gui";

    public GameEffectRemoteGui(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator) {
        return true;
    }

    @Override
    public void render(@NotNull VRRenderPass renderPass, @NotNull PoseStack poseStack, float partialTicks) {
        if (VRClientPlayers.getRemotePlayers().isEmpty()) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        var localRenderPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);
        var eyePos = RenderPoseHelper.getCameraPosition(renderPass, localRenderPose);

        for (var remotePlayer : VRClientPlayers.getRemotePlayers()) {
            if (!remotePlayer.isGuiOpened()) continue;

            VRPose hmdPose = remotePlayer.getPoseData(PlayerPoseType.RENDER).getHmd();

            poseStack.pushPose();
            poseStack.setIdentity();
            RenderPoseHelper.applyCameraOrientation(renderPass, poseStack);

            Vector3f forward = hmdPose.getDirection().mul(0.45f, new Vector3f());
            Vector3f indicatorPos = new Vector3f(hmdPose.getPosition()).add(forward);

            poseStack.translate(indicatorPos.x - eyePos.x(), indicatorPos.y - eyePos.y(), indicatorPos.z - eyePos.z());
            poseStack.mulPoseMatrix((Matrix4f) hmdPose.getRotation());
            RenderHelper.renderDisplayQuad(
                    poseStack.last().pose(),
                    new AtumColorImmutable(40, 45, 60, 140),
                    1.6f, 0.9f,
                    0.4f
            );
            // todo: add Visor's logo
            poseStack.popPose();
        }

        RenderSystem.disableBlend();
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}