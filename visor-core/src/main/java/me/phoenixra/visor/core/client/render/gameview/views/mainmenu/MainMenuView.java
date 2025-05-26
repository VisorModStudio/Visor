package me.phoenixra.visor.core.client.render.gameview.views.mainmenu;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.phoenixra.visor.api.client.data.VRPoseStage;
import me.phoenixra.visor.api.client.render.gameview.VRGameViewBase;
import me.phoenixra.visor.api.client.render.gameview.IVRGameViewHandler;
import me.phoenixra.visor.api.client.render.gameview.annotations.RegisterVRGameView;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.data.VRClientPose;
import me.phoenixra.visor.core.client.render.helpers.RenderHelper;
import me.phoenixra.visor.core.client.render.VRRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import me.phoenixra.visor.core.client.ClientContext;

@RegisterVRGameView
public class MainMenuView extends VRGameViewBase {

    public MainMenuView(@NotNull VisorAddon owner) {
        super(owner, IVRGameViewHandler.VIEW_MAIN_MENU);
    }

    @Override
    public void onEnter() {

    }

    @Override
    public void onExit() {

    }


    @Override
    public void tick() {

    }

    @Override
    public void render(PoseStack poseStack, float partialTicks) {
        RenderHelper.applyDisplayPose(VRRenderState.getCurrentVRDisplay(), poseStack);

        renderPanorama(poseStack);


        ClientContext.guiManager.renderGUI(
                poseStack,
                partialTicks,
                true
        );

        if(ClientContext.properties.isVrHandsAllowed()) {
            ClientContext.handRenderer.renderSimpleHands(
                    poseStack, partialTicks,
                    true, true
            );
        }

    }

    private static void renderPanorama(PoseStack poseStack){

        VRClientPose renderPose = ClientContext.player
                .getPose(VRPoseStage.RENDER);
        poseStack.pushPose();
        Vec3 eye = RenderHelper.getCameraPosition(
                VRRenderState.getCurrentVRDisplay(),
                renderPose
        );
        Vec3 origin = renderPose.getOrigin();
        poseStack.translate(
                origin.x - eye.x,
                origin.y - eye.y,
                origin.z - eye.z
        );

        poseStack.mulPose(
                Axis.YN.rotation(
                        -renderPose.getRotationYaw()
                )
        );

        VRMenuPanorama.renderMenuPanorama(poseStack);
        poseStack.popPose();
    }

}
