package me.phoenixra.visor.core.client.render.decoration.decorators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRDecorator;
import me.phoenixra.visor.api.common.addon.component.ComponentPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.player.pose.LocalPlayerPose;
import me.phoenixra.visor.core.client.render.decoration.decorators.mainmenu.VRMenuPanorama;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import me.phoenixra.visor.core.client.render.VRRenderState;
import org.jetbrains.annotations.NotNull;

import me.phoenixra.visor.core.client.ClientContext;

import java.util.List;

@RegisterVRDecorator
public class DecoratorMainMenu extends VRDecorator {
    public static final String ID = "main_menu";



    public DecoratorMainMenu(@NotNull VisorAddon owner) {
        super(owner, ID);
    }



    @Override
    public void tick() {

    }

    @Override
    public void render(PoseStack poseStack, float partialTicks) {
        RenderPoseHelper.applyCameraOrientation(VRRenderState.getCameraType(), poseStack);

        renderPanorama(poseStack);


        ClientContext.guiManager.renderGUI(
                poseStack,
                partialTicks
        );

        ClientContext.handRenderer.renderGuiHands(
                this,
                poseStack, partialTicks,
                true, true
        );

        ClientContext.decorationRenderer.renderGameEffects(
                this,
                poseStack, partialTicks
        );
    }

    private static void renderPanorama(PoseStack poseStack){

        LocalPlayerPose renderPose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RENDER);
        poseStack.pushPose();
        var eye = RenderPoseHelper.getCameraPosition(
                VRRenderState.getCameraType(),
                renderPose
        );
        var origin = renderPose.getOrigin();
        poseStack.translate(
                origin.x() - eye.x(),
                origin.y() - eye.y(),
                origin.z() - eye.z()
        );

        poseStack.mulPose(
                Axis.YN.rotation(
                        -renderPose.getRotationY()
                )
        );

        VRMenuPanorama.renderMenuPanorama(poseStack);
        poseStack.popPose();
    }

    @Override
    public boolean canActivate() {
        return VRRenderState.isInMainMenu();
    }

    @Override
    public List<String> gameEffects() {
        return List.of();
    }

    @Override
    public List<String> handEffects() {
        return List.of();
    }

    @Override
    public @NotNull ComponentPriority getPriority() {
        return ComponentPriority.LOW;
    }
}
