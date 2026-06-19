package org.vmstudio.visor.core.client.render.decoration.decorators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRDecorator;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.player.pose.LocalPlayerPose;
import org.vmstudio.visor.core.client.render.decoration.decorators.mainmenu.VRMenuFloor;
import org.vmstudio.visor.core.client.render.decoration.decorators.mainmenu.VRMenuPanorama;
import org.vmstudio.visor.core.client.render.decoration.decorators.mainmenu.VRMenuSky;
import org.vmstudio.visor.core.client.render.decoration.decorators.mainmenu.VRMenuSkyCanvas;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.options.enums.MainMenuSceneMode;
import org.jetbrains.annotations.NotNull;

import org.vmstudio.visor.core.client.ClientContext;

import java.util.List;

@RegisterVRDecorator
public class DecoratorMainMenu extends VRDecorator {
    public static final String ID = "main_menu";

    public DecoratorMainMenu(@NotNull VisorAddon owner) {
        super(owner, ID);
    }

    @Override
    public void init() {
        super.init();
        VRMenuSky.reset();
        VRMenuSkyCanvas.init();
    }

    @Override
    public void tick() {
    }

    @Override
    public void setupRendering(@NotNull PoseStack poseStack, float partialTicks) {
        RenderPoseHelper.applyCameraOrientation(VRRenderState.getRenderPass(), poseStack);
        renderScene(poseStack);
    }

    private static void renderScene(PoseStack poseStack){
        LocalPlayerPose renderPose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RENDER);
        poseStack.pushPose();
        var eye = RenderPoseHelper.getCameraPosition(
                VRRenderState.getRenderPass(),
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

        boolean skyScene = VRClientSettings.getMainMenuScene() != MainMenuSceneMode.PANORAMA;

        if (skyScene) {
            VRMenuSky.renderFirst(poseStack);
        } else {
            VRMenuPanorama.render(poseStack);
        }

        VRMenuFloor.render(poseStack);

        if (skyScene) {
            VRMenuSky.renderLast(poseStack);
        }

        poseStack.popPose();
    }

    @Override
    public boolean canActivate() {
        return VisorAPI.clientState().sceneType().isMainMenu();
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