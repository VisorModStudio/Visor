package me.phoenixra.visor.core.client.gui.overlays.builtin.settings;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsMisc;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayScreen;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;


public class VROverlayDemo extends VROverlayScreen {
    public static final String ID = "demo";

    private static final int EDGE_LINE_SIZE = 2;


    private final Vector3f movingPosOffset = new Vector3f(0,0,-0.3f);
    private final Vector3f movingRotationOffset = new Vector3f(0,0,0);


    private VROverlay target;
    private OverlayOptionsPose targetPoseOptions;
    private OverlayOptionsMisc targetOptionsGlobal;

    private boolean appliedPose;

    @Getter @Setter
    public boolean emulatingPose;

    @Nullable @Getter
    private PoseAnchor movingByAnchor;

    private float overlayScale = 1.0f;
    public VROverlayDemo(@NotNull VisorAddon owner,
                         @NotNull String id) {
        super(owner, id, ElementPriority.HIGHEST, 1.0f);

    }


    @Override
    protected void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int startX = 0;
        int startY = 0;

        int width = this.width;
        int height = this.height;

        //Screen Edge
        renderOutline(guiGraphics, startX, startY, width, height, AtumColor.RED.toInt());

        //MOUSE bounds outline
        startX = cursorBoundsX;
        startY = cursorBoundsY;

        width = cursorBoundsWidth;
        height = cursorBoundsHeight;

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
        renderOutline(guiGraphics, startX, startY, width, height, AtumColor.GREEN.toInt());


    }
    private void renderOutline(GuiGraphics guiGraphics,
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
        if(target != null){
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
            }else if(targetPoseOptions.isTickModelView()) {

                if(!emulatingPose) return;

                VROverlayHelper.applyPose(
                        this,
                        targetPoseOptions.getPositionAnchor(),
                        targetPoseOptions.getRotationAnchor(),
                        overlayScale,
                        targetPoseOptions.isAimRotation(),
                        targetPoseOptions.getPosOffset(),
                        targetPoseOptions.getRotationOffsetVec()
                );
            }else if(!appliedPose){
                VROverlayHelper.applyPose(
                        this,
                        targetPoseOptions.getPositionAnchor(),
                        targetPoseOptions.getRotationAnchor(),
                        overlayScale,
                        targetPoseOptions.isAimRotation(),
                        targetPoseOptions.getPosOffset(),
                        targetPoseOptions.getRotationOffsetVec()
                );
                appliedPose = true;
            }
        }
    }

    @Override
    public void onDisable() {
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

        if(target instanceof VROverlayScreen overlayScreen){
            cursorBoundsX = overlayScreen.getCursorBoundsX();
            cursorBoundsY = overlayScreen.getCursorBoundsY();
            cursorBoundsWidth = overlayScreen.getCursorBoundsWidth();
            cursorBoundsHeight = overlayScreen.getCursorBoundsHeight();
        }else{
            cursorBoundsX = -1;
            cursorBoundsY = -1;
            cursorBoundsWidth = -1;
            cursorBoundsHeight = -1;
        }

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

        PoseAnchor posAnchor = targetPoseOptions.getPositionAnchor();
        emulatingPose = false;
        movingByAnchor = posAnchor == PoseAnchor.MAIN_HAND
                ? PoseAnchor.OFFHAND
                : PoseAnchor.MAIN_HAND;
        target.setForcedAnchor(movingByAnchor);

        ClientContext.cursorHandler.setCursorHand(
                movingByAnchor == PoseAnchor.OFFHAND
                ? ControllerHand.OFFHAND : ControllerHand.MAIN
        );
    }

    public void stopAnchorMoving(){
        if(!isEnabled()) return;

        applyNewOffset();

        movingByAnchor = null;
        target.setForcedAnchor(null);
    }

    public void applyNewOffset(){
        if(!isEnabled()) return;
        emulatingPose = true;

        PoseData renderPose = ClientContext.player
                .getPoseData(PoseDataType.RENDER);

        PoseAnchor posAnchor = targetPoseOptions.getPositionAnchor();

        PoseElement elementAnchorPos = posAnchor.getSupplier()
                .apply(renderPose);
        var anchorPosition = elementAnchorPos.getPosition();

        Vector3f offsetPos = elementAnchorPos
                .reverseCustomVector(
                        getPose().getPosition().sub(anchorPosition, new Vector3f())
                ).div(
                        renderPose.getWorldScale()
                );

        targetPoseOptions.setFormulaPosX(
                format(offsetPos.x)
        );
        targetPoseOptions.setFormulaPosY(
                format(offsetPos.y)
        );
        targetPoseOptions.setFormulaPosZ(
                format(offsetPos.z)
        );

        if(targetPoseOptions.isAimRotation()){
            //applying any other rotation for aimed is awkward
            //in that case
            targetPoseOptions.setFormulaRotationX("0");
            targetPoseOptions.setFormulaRotationY("0");
            targetPoseOptions.setFormulaRotationZ("0");
            targetPoseOptions.update(true);
            return;
        }

        PoseAnchor rotationAnchor = targetPoseOptions.getRotationAnchor();

        PoseElement elementAnchorRot = rotationAnchor.getSupplier()
                .apply(renderPose);
        var anchorRotation = elementAnchorRot.getRotation();

        Vector3f rotationOffset = rotationAnchor.reverseAnchoredRotation(
                anchorRotation,
                getPose().getRotation()

        );

        targetPoseOptions.setFormulaRotationX(
                format(rotationOffset.x)
        );
        targetPoseOptions.setFormulaRotationY(
                format(rotationOffset.y)
        );
        targetPoseOptions.setFormulaRotationZ(
                format(rotationOffset.z)
        );

        targetPoseOptions.update(true);

    }

    private String format(float value){
        String formatResult = String.format("%.3f", value);
        //fixes weird issue in some cases
        formatResult = formatResult.replace(",",".");

        return formatResult.endsWith("000") ? String.valueOf((int) value) : formatResult;
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
    public boolean mouseClicked(double mouseX, double mouseY, int buttonType) {
        if(movingByAnchor == null) return true;
        stopAnchorMoving();
        return true;
    }


    @Override
    public int getRequestedWidth() {
        if(target == null){
            return super.getRequestedWidth();
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
