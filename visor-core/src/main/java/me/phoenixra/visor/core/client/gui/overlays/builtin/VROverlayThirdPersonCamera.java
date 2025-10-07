package me.phoenixra.visor.core.client.gui.overlays.builtin;

import lombok.Getter;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class VROverlayThirdPersonCamera extends VROverlayScreen {
    public static final String ID = "third_person_camera";

    private final GuiTexture cameraTexture = new GuiTexture(
            new ResourceLocation(
                    VisorAPI.MOD_ID, "textures/gui/overlays/camera.png"
            )
    );

    private final Vector3f posDragOffset = new Vector3f(0, 0, -0.3f);
    private final Vector3f rotationDragOffset = new Vector3f(0, 0, 0);


    private float offsetZWaiting;
    private float preTickOffsetZ = -0.3f;
    private float postTickOffsetZ = -0.3f;

    @Getter
    boolean changingPosition;

    public VROverlayThirdPersonCamera(@NotNull VisorAddon owner,
                                      @NotNull String id) {
        super(owner, id, ElementPriority.HIGHER, 0.35f);
        setEnabled(true);
    }

    @Override
    protected void onTick() {
        preTickOffsetZ = postTickOffsetZ;
        if(offsetZWaiting != 0){
            postTickOffsetZ += offsetZWaiting;
            if(postTickOffsetZ >= -0.1f){
                postTickOffsetZ = -0.1f;
            }
        }
        offsetZWaiting = 0;
    }


    @Override
    protected void onRender(GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {

        cameraTexture.blit(
                guiGraphics,
                width/2-256/2,height/2-256/2,
                256,256
        );

    }

    @Override
    public void onUpdatePose(float partialTicks) {
        if(changingPosition){
            PoseAnchor anchor = ClientContext.cursorHandler
                    .getCursorHand() == ControllerHand.MAIN ?
                    PoseAnchor.MAIN_HAND : PoseAnchor.OFFHAND;

            posDragOffset.z = Mth.lerp(partialTicks, preTickOffsetZ, postTickOffsetZ);

            VROverlayHelper.applyPose(
                    this,
                    anchor,
                    anchor,
                    getPose().getScale(),
                    true,
                    posDragOffset,
                    rotationDragOffset
            );
            updateCameraPose(false);

            return;
        }
        var renderData = ClientContext.player.getPoseData(PoseDataType.RENDER);
        var camPosition = renderData
                .getThirdPersonCamera().getPosition();
        var camRotation =  renderData
                .getThirdPersonCamera().getRotation();
        getPose().update(
                camPosition,
                camRotation.rotateY((float) Math.PI, new Matrix4f()),
                getPose().getScale()
        );
    }

    public void setChangingPosition(boolean flag) {
        if(this.changingPosition && !flag){
            //make sure pose is valid
            updatePose(1);

            updateCameraPose(true);

            posDragOffset.z = -0.3f;
            preTickOffsetZ = -0.3f;
            postTickOffsetZ = -0.3f;
            offsetZWaiting = 0;
        }
        this.changingPosition = flag;
    }

    private void updateCameraPose(boolean save){
        var roomData = ClientContext.player.getPoseData(PoseDataType.ROOM);
        var newPosition = roomData
                .convertPositionFrom(
                        PoseDataType.RENDER,
                        getPose().getPosition()
                );
        Quaternionfc newRotation = roomData.convertRotationFrom(
                PoseDataType.RENDER,
                getPose().getRotation()
        ).getUnnormalizedRotation(new Quaternionf()).rotateY((float) Math.PI);

        VRClientSettings.updateThirdPersonCamera(
                newPosition,
                newRotation,
                save
        );
    }

    @Override
    public boolean mouseScrolled(double mouseX,
                                 double mouseY,
                                 double scrollDelta) {
        if(this.changingPosition){
            offsetZWaiting += (float) -scrollDelta * 0.01f;
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonType) {
        if(this.changingPosition){
            setChangingPosition(false);
        }
        return true;
    }


    @Override
    public boolean supportsCursor() {
        return this.changingPosition;
    }

    @Override
    protected boolean updateVisibility() {
        return VRRenderState.getVRWorldDisplays().contains(VRDisplay.THIRD_PERSON);
    }

    @Override
    public boolean isInViewDistance() {
        return true;
    }




    @Override
    public @NotNull Component getName() {
        return Component.translatable("visor.overlay.%s.name".formatted(getId()));
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.translatable("visor.overlay.%s.description".formatted(getId()));
    }


}
