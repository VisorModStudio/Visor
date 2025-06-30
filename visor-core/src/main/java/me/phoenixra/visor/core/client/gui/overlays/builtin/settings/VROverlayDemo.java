package me.phoenixra.visor.core.client.gui.overlays.builtin.settings;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.template.options.types.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.overlay.template.options.types.OverlayOptionsLocation;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.overlay.template.VROverlayTemplate;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector3f;


public class VROverlayDemo extends VROverlayScreen {
    public static final String ID = "demo";

    private static final int EDGE_LINE_SIZE = 2;


    private final Vector3f movingPosOffset = new Vector3f(0,0,-0.3f);
    private final Vector3f movingRotationOffset = new Vector3f(0,0,0);


    private VROverlayTemplate demonstrating;
    private OverlayOptionsLocation demoModelViewOptions;
    private OverlayOptionsGlobal demoOptionsGlobal;

    private boolean appliedModelView;

    @Getter @Setter
    public boolean emulatingModelView;

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
        startX = cursorEdgeX;
        startY = cursorEdgeY;

        width = cursorEdgeWidth;
        height = cursorEdgeHeight;

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
    public void onDisable() {
        demonstrating = null;
        demoModelViewOptions = null;
        appliedModelView = false;
    }

    @Override
    public void updatePose(float partialTicks) {
        if(!demonstrating.isVisible()
                && demoOptionsGlobal != null
                && demoOptionsGlobal.getUpdateOptionsType() == OverlayOptionsGlobal.UpdateOptionsType.FRAME) {
            //since demonstrating overlay is not visible
            //its options are not handled on render tick
            //So, we have to do that ourselves to ensure modelView is valid
            demonstrating.getTemplateOptions().forEach(
                    it->it.update(false)
            );
        }
        if(demonstrating != null){
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
            }else if(demoModelViewOptions.isTickModelView()) {

                if(!emulatingModelView) return;

                VROverlayHelper.applyPose(
                        this,
                        demoModelViewOptions.getPositionAnchor(),
                        demoModelViewOptions.getRotationAnchor(),
                        overlayScale,
                        demoModelViewOptions.isAimRotation(),
                        demoModelViewOptions.getPosOffset(),
                        demoModelViewOptions.getRotationOffsetVec()
                );
            }else if(!appliedModelView){
                VROverlayHelper.applyPose(
                        this,
                        demoModelViewOptions.getPositionAnchor(),
                        demoModelViewOptions.getRotationAnchor(),
                        overlayScale,
                        demoModelViewOptions.isAimRotation(),
                        demoModelViewOptions.getPosOffset(),
                        demoModelViewOptions.getRotationOffsetVec()
                );
                appliedModelView = true;
            }
        }
    }

    public void showDemo(@NotNull VROverlayTemplate overlay){
        setEnabled(false);

        demonstrating = overlay;
        demoModelViewOptions = demonstrating.getTemplateOption(OverlayOptionsLocation.class);
        demoOptionsGlobal = demonstrating.getTemplateOption(OverlayOptionsGlobal.class);

        overlayScale = demonstrating.getPose().getScale();

        if(demonstrating instanceof VROverlayScreen overlayScreen){
            cursorEdgeX = overlayScreen.getCursorEdgeX();
            cursorEdgeY = overlayScreen.getCursorEdgeY();
            cursorEdgeWidth = overlayScreen.getCursorEdgeWidth();
            cursorEdgeHeight = overlayScreen.getCursorEdgeHeight();
        }else{
            cursorEdgeX = -1;
            cursorEdgeY = -1;
            cursorEdgeWidth = -1;
            cursorEdgeHeight = -1;
        }

        setEnabled(demoModelViewOptions != null);
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
        appliedModelView = true;
    }

    public void startMovingByAnchor(){
        if(!isEnabled()) return;

        PoseAnchor posAnchor = demoModelViewOptions.getPositionAnchor();
        emulatingModelView = false;
        movingByAnchor = posAnchor == PoseAnchor.MAIN_HAND
                ? PoseAnchor.OFFHAND
                : PoseAnchor.MAIN_HAND;
        demonstrating.setDemoAnchor(movingByAnchor);

        ClientContext.cursorHandler.setCursorHand(
                movingByAnchor == PoseAnchor.OFFHAND
                ? ControllerHand.OFFHAND : ControllerHand.MAIN
        );
    }

    public void stopAnchorMoving(){
        if(!isEnabled()) return;

        applyNewOffset();

        movingByAnchor = null;
        demonstrating.setDemoAnchor(null);
    }

    public void applyNewOffset(){
        if(!isEnabled()) return;
        PoseData renderPose = ClientContext.player
                .getPose(PoseDataType.RENDER);
        emulatingModelView = true;

        PoseAnchor posAnchor = demoModelViewOptions.getPositionAnchor();
        PoseAnchor rotationAnchor = demoModelViewOptions.getRotationAnchor();

        PoseElement componentAnchorPos = posAnchor.getSupplier()
                .apply(renderPose);
        PoseElement componentAnchorRot = rotationAnchor.getSupplier()
                .apply(renderPose);
        var componentPos = componentAnchorPos.getPosition();
        Matrix4fc componentRotation = componentAnchorRot.getRotation();

        Vector3f offsetPos = componentAnchorPos
                .reverseCustomVector(
                        getPose().getPosition().sub(componentPos, new Vector3f())
                ).div(
                        renderPose.getWorldScale()
                );

        demoModelViewOptions.setFormulaPosX(
                format(offsetPos.x)
        );
        demoModelViewOptions.setFormulaPosY(
                format(offsetPos.y)
        );
        demoModelViewOptions.setFormulaPosZ(
                format(offsetPos.z)
        );

        if(demoModelViewOptions.isAimRotation()){
            //applying any other rotation for aimed is awkward
            //in that case
            demoModelViewOptions.setFormulaRotationX("0");
            demoModelViewOptions.setFormulaRotationY("0");
            demoModelViewOptions.setFormulaRotationZ("0");
            demoModelViewOptions.update(true);
            return;
        }

        Vector3f rotationOffset = rotationAnchor.reverseAnchoredRotation(
                componentRotation,
                getPose().getRotation()
        );

        demoModelViewOptions.setFormulaRotationX(
                format(rotationOffset.x)
        );
        demoModelViewOptions.setFormulaRotationY(
                format(rotationOffset.y)
        );
        demoModelViewOptions.setFormulaRotationZ(
                format(rotationOffset.z)
        );

        demoModelViewOptions.update(true);

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

}
