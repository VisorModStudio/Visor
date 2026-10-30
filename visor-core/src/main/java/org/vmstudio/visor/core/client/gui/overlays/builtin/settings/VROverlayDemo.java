package org.vmstudio.visor.core.client.gui.overlays.builtin.settings;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.player.pose.PoseAnchor;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsMisc;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McGuiUtils;
import org.vmstudio.visor.core.client.ClientContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;


public class VROverlayDemo extends VROverlayScreen {
    public static final String ID = "demo";

    private static final int EDGE_LINE_SIZE = 2;


    private final Vector3f movingPosOffset = new Vector3f(0,0,-0.3f);
    private final Quaternionf movingRotationOffset = new Quaternionf();


    private VROverlay target;
    private OverlayOptionsPose targetPoseOptions;
    private OverlayOptionsMisc targetOptionsGlobal;

    private boolean appliedPose;

    @Getter @Setter
    private boolean emulatingPose;

    @Nullable @Getter
    private PoseAnchor movingByAnchor;

    private float overlayScale = 1.0f;

    public VROverlayDemo(@NotNull VisorAddon owner,
                         @NotNull String id) {
        super(owner, id, ComponentPriority.HIGHEST, 1.0f);
    }


    @Override
    protected void onRender(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int startX = 0;
        int startY = 0;

        int width = this.width;
        int height = this.height;

        //Screen Edge
        renderOutline(guiGraphics, startX, startY, width, height, AtumColor.RED.asInt());

        if(!(target instanceof VROverlayScreen overlayScreen)){
            return;
        }

        //MOUSE bounds outline
        startX = overlayScreen.getCursorBoundsX();
        startY = overlayScreen.getCursorBoundsY();

        width = overlayScreen.getCursorBoundsWidth();
        height = overlayScreen.getCursorBoundsHeight();

        if(startX == -1
                || startY == -1
                || width == -1
                || height == -1){
            return;
        }
        if(startX == 0
                && startY == 0
                && width == this.width
                && height == this.height){
            return;
        }
        renderOutline(guiGraphics, startX, startY, width, height, AtumColor.GREEN.asInt());


    }


    @Override
    protected void onTick() {
        if(movingByAnchor != null){
            ClientContext.cursorHandler.setForceFocused(this);
        }
    }

    @Override
    public void onUpdatePose(float partialTicks) {
        if(!target.isVisible()
                && targetOptionsGlobal != null
                && targetOptionsGlobal.getOptionsUpdaterType() == OverlayOptionsMisc.OptionsUpdaterType.FRAME) {
            //since demonstrating overlay is not visible
            //its options are not handled on render tick
            //So, we have to do that ourselves to ensure modelView is valid
            target.getOptions().forEach(
                    it->it.update(false)
            );
        }
        overlayScale = targetPoseOptions.getScale();
        if(movingByAnchor != null){
            VROverlayHelper.applyPose(
                    this,
                    movingByAnchor,
                    movingByAnchor,
                    overlayScale,
                    false,
                    movingPosOffset,
                    movingRotationOffset
            );
        }else if(targetPoseOptions.isTickPose()) {

            if(!emulatingPose) return;

            VROverlayHelper.applyPose(
                    this,
                    targetPoseOptions.getPositionAnchor(),
                    targetPoseOptions.getRotationAnchor(),
                    targetPoseOptions.getScale(),
                    targetPoseOptions.isAimedRotation(),
                    targetPoseOptions.getPositionOffset(),
                    targetPoseOptions.getRotationOffset()
            );
        }else if(!appliedPose){
            VROverlayHelper.applyPose(
                    this,
                    targetPoseOptions.getPositionAnchor(),
                    targetPoseOptions.getRotationAnchor(),
                    targetPoseOptions.getScale(),
                    targetPoseOptions.isAimedRotation(),
                    targetPoseOptions.getPositionOffset(),
                    targetPoseOptions.getRotationOffset()
            );
            appliedPose = true;
        }
    }

    @Override
    public void onDisable() {
        if(movingByAnchor != null){
            endMovingByAnchor();
        }
        target = null;
        targetPoseOptions = null;
        appliedPose = false;
    }



    public void showDemo(@NotNull VROverlay overlay){
        setEnabled(false);

        target = overlay;
        targetPoseOptions = target.getOption(OverlayOptionsPose.ID, OverlayOptionsPose.class);
        targetOptionsGlobal = target.getOption(OverlayOptionsMisc.ID, OverlayOptionsMisc.class);

        overlayScale = target.getPose().getScale();


        setEnabled(targetPoseOptions != null);
    }


    public void teleportToHMD(){
        if(!isEnabled()) return;
        VROverlayHelper.applyPose(
                this,
                PoseAnchor.HMD,
                PoseAnchor.HMD,
                overlayScale,
                true,
                new Vector3f(0,-0.5f,-0.6f),
                new Vector3f()
        );
        appliedPose = true;
    }

    public void startMovingByAnchor(){
        if(!isEnabled()) return;
        if(movingByAnchor != null) return;

        PoseAnchor posAnchor = targetPoseOptions.getPositionAnchor();
        emulatingPose = false;
        movingByAnchor = posAnchor == PoseAnchor.MAIN_HAND
                ? PoseAnchor.OFFHAND
                : PoseAnchor.MAIN_HAND;
        target.setForcedAnchor(
                movingByAnchor,
                movingPosOffset,
                new Matrix4f().rotation(movingRotationOffset)
        );

        ClientContext.cursorHandler.setCursorHand(
                movingByAnchor == PoseAnchor.OFFHAND
                ? HandType.OFFHAND : HandType.MAIN
        );
        ClientContext.cursorHandler.setForceFocused(this);
    }

    public void stopMovingByAnchor(){
        if(!isEnabled()) return;
        if(movingByAnchor == null) return;

        applyNewOffset();
        endMovingByAnchor();
    }

    private void endMovingByAnchor(){
        movingByAnchor = null;
        target.setForcedAnchor(null);
        if(ClientContext.cursorHandler.getForceFocused() == this){
            ClientContext.cursorHandler.setForceFocused(null);
        }
    }

    public void applyNewOffset(){
        if(!isEnabled()) return;
        emulatingPose = true;

        VRPlayerPoseClient renderPose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RENDER);

        PoseAnchor posAnchor = targetPoseOptions.getPositionAnchor();

        VRPose posAnchorPose = posAnchor.getSupplier()
                .apply(renderPose);
        var anchorPosition = posAnchorPose.getPosition();

        Vector3f offsetPos = posAnchorPose
                .inverseTransformDirection(
                        getPose().getPosition().sub(anchorPosition, new Vector3f())
                ).div(
                        renderPose.getWorldScale()
                );

        targetPoseOptions.setPositionOffset(
                offsetPos
        );

        if(targetPoseOptions.isAimedRotation()){
            //applying any other rotation for aimed is awkward
            //in that case
            targetPoseOptions.setRotationOffset(
                    0,0,0
            );

            targetPoseOptions.update(true);
            return;
        }

        PoseAnchor rotationAnchor = targetPoseOptions.getRotationAnchor();

        VRPose rotationAnchorPose = rotationAnchor.getSupplier()
                .apply(renderPose);
        Matrix4fc anchorRotation = rotationAnchorPose.getRotation();

        Matrix4f rotationOffsetMatrix = anchorRotation.invert(new Matrix4f())
                .mul(getPose().getRotation(), new Matrix4f());

        targetPoseOptions.setRotationOffset(rotationOffsetMatrix);

        targetPoseOptions.update(true);
    }

    private void renderOutline(GuiGraphicsExtractor guiGraphics,
                               int x, int y,
                               int width, int height,
                               int color){
        int endX = x+width;
        int endY = y+height;
        // Top edge
        guiGraphics.fill(
                x, y,
                endX, y+EDGE_LINE_SIZE,
                color
        );
        // Bottom edge
        guiGraphics.fill(
                x, endY - EDGE_LINE_SIZE,
                endX, endY,
                color
        );
        // Left edge
        guiGraphics.fill(
                x, y+EDGE_LINE_SIZE,
                x+EDGE_LINE_SIZE, endY - EDGE_LINE_SIZE,
                color
        );
        // Right edge
        guiGraphics.fill(
                endX - EDGE_LINE_SIZE, y+EDGE_LINE_SIZE,
                endX, endY - EDGE_LINE_SIZE,
                color
        );
    }



    public boolean isMovingByAnchor(){
        return movingByAnchor != null;
    }
    @Override
    public boolean supportsCursor() {
        return movingByAnchor != null;
    }

    @Override
    public boolean updateVisibility() {
        return true;
    }

    @Override
    protected boolean onMouseClicked(double mouseX, double mouseY, int buttonType) {
        if(movingByAnchor == null) return true;
        stopMovingByAnchor();
        return true;
    }


    @Override
    public void updateSize() {
        if(target instanceof VROverlayScreen overlayScreen
                && overlayScreen.getGuiScaleFactor() > 0){
            guiScaleFactor = overlayScreen.getGuiScaleFactor();
            McGuiUtils.initScreen(
                    this,
                    getRequestedWidthScaled(),
                    getRequestedHeightScaled()
            );
            return;
        }
        super.updateSize();
    }

    @Override
    public int getRequestedWidth() {
        if(target == null){
            return super.getRequestedWidth();
        }
        if(target instanceof VROverlayScreen overlayScreen){
            return overlayScreen.getRequestedWidth();
        }
        var renderTarget = target.getRenderTarget();
        if(renderTarget == null){
            return super.getRequestedWidth();
        }
        return renderTarget.width;
    }

    @Override
    public int getRequestedHeight() {
        if(target == null){
            return super.getRequestedHeight();
        }
        if(target instanceof VROverlayScreen overlayScreen){
            return overlayScreen.getRequestedHeight();
        }
        var renderTarget = target.getRenderTarget();
        if(renderTarget == null){
            return super.getRequestedHeight();
        }
        return renderTarget.height;
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
