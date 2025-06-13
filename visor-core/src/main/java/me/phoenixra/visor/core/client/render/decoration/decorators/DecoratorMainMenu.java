package me.phoenixra.visor.core.client.render.decoration.decorators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRDecorator;
import me.phoenixra.visor.api.common.addon.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.data.PoseDataImpl;
import me.phoenixra.visor.core.client.render.decoration.decorators.mainmenu.VRMenuPanorama;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import me.phoenixra.visor.core.client.render.VRRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import me.phoenixra.visor.core.client.ClientContext;

@RegisterVRDecorator
public class DecoratorMainMenu extends VRDecorator {
    public static final String ID = "main_menu";



    public DecoratorMainMenu(@NotNull VisorAddon owner) {
        super(owner, ID);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onExit() {

    }


    @Override
    public void tick() {

    }

    @Override
    public void render(PoseStack poseStack, float partialTicks) {
        RenderPoseHelper.applyDisplayOrientation(VRRenderState.getCurrentVRDisplay(), poseStack);

        renderPanorama(poseStack);


        ClientContext.guiManager.renderGUI(
                poseStack,
                partialTicks,
                true
        );

        ClientContext.handRenderer.renderGuiHands(
                poseStack, partialTicks,
                true, true
        );

        ClientContext.decorationRenderer.renderGameEffects(
                poseStack, partialTicks
        );
    }

    private static void renderPanorama(PoseStack poseStack){

        PoseDataImpl renderPose = ClientContext.player
                .getPose(PoseType.RENDER);
        poseStack.pushPose();
        Vec3 eye = RenderPoseHelper.getCameraPosition(
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
    public @NotNull ElementPriority getPriority() {
        return ElementPriority.LOW;
    }
}
