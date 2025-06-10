package me.phoenixra.visor.core.client.gui.overlays.builtin.settings;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.gui.overlay.ModelViewAnchor;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsModelView;
import me.phoenixra.visor.api.client.gui.overlay.types.VROverlayScreen;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.List;



public class VROverlayDemo extends VROverlayScreen {
    public static final String ID = "demo";

    private static final int EDGE_LINE_SIZE = 2;


    private final Vector3f movingPosOffset = new Vector3f(0,0,-0.3f);
    private final Vector3f movingRotationOffset = new Vector3f(0,0,0);


    private VROverlay demonstrating;
    private OverlayOptionsModelView demoModelViewOptions;
    private OverlayOptionsGlobal demoOptionsGlobal;

    private boolean appliedModelView;

    @Getter @Setter
    public boolean emulatingModelView;

    @Nullable @Getter
    private ModelViewAnchor movingByAnchor;
    public VROverlayDemo(@NotNull VisorAddon owner,
                         @NotNull String id) {
        super(owner, id);

        //have to be drawn on top of everything
        setPriority(ElementPriority.HIGHEST);
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
        startX = mouseEdgeX;
        startY = mouseEdgeY;

        width = mouseEdgeWidth;
        height = mouseEdgeHeight;

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
    public void onTick() {

    }


    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        demonstrating = null;
        demoModelViewOptions = null;
        appliedModelView = false;
    }

    @Override
    public void applyModelView(float partialTick) {
        if(!demonstrating.isVisible()
                && demoOptionsGlobal != null
                && demoOptionsGlobal.getUpdateOptionsType() == OverlayOptionsGlobal.UpdateOptionsType.FRAME) {
            //since demonstrating overlay is not visible
            //its options are not handled on render tick
            //So, we have to do that ourselves to ensure modelView is valid
            demonstrating.getOptionsList().forEach(
                    it->it.update(false)
            );
        }
        if(demonstrating != null){
            if(movingByAnchor != null){
                VROverlayHelper.applyModelView(
                        this,
                        movingByAnchor,
                        movingByAnchor,
                        false,
                        movingPosOffset,
                        movingRotationOffset
                );
            }

            if(demoModelViewOptions.isTickModelView()) {

                if(!emulatingModelView) return;

                VROverlayHelper.applyModelView(
                        this,
                        demoModelViewOptions.getPositionAnchor(),
                        demoModelViewOptions.getRotationAnchor(),
                        demoModelViewOptions.isAimRotation(),
                        demoModelViewOptions.getPosOffset(),
                        demoModelViewOptions.getRotationOffsetVec()
                );
            }else if(!appliedModelView){
                VROverlayHelper.applyModelView(
                        this,
                        demoModelViewOptions.getPositionAnchor(),
                        demoModelViewOptions.getRotationAnchor(),
                        demoModelViewOptions.isAimRotation(),
                        demoModelViewOptions.getPosOffset(),
                        demoModelViewOptions.getRotationOffsetVec()
                );
                appliedModelView = true;
            }
        }
    }

    public void showDemo(@NotNull VROverlay overlay){
        setEnabled(false);

        demonstrating = overlay;
        demoModelViewOptions = demonstrating.getOptionCategory(OverlayOptionsModelView.class);
        demoOptionsGlobal = demonstrating.getOptionCategory(OverlayOptionsGlobal.class);

        setOverlayScale(demonstrating.getOverlayScale());

        if(demonstrating instanceof VROverlayScreen overlayScreen){
            mouseEdgeX = overlayScreen.getMouseEdgeX();
            mouseEdgeY = overlayScreen.getMouseEdgeY();
            mouseEdgeWidth = overlayScreen.getMouseEdgeWidth();
            mouseEdgeHeight = overlayScreen.getMouseEdgeHeight();
        }else{
            mouseEdgeX = -1;
            mouseEdgeY = -1;
            mouseEdgeWidth = -1;
            mouseEdgeHeight = -1;
        }

        setEnabled(demoModelViewOptions != null);
    }

    public void teleportToHMD(){
        if(!isEnabled()) return;
        VROverlayHelper.applyModelView(
                this,
                ModelViewAnchor.HMD,
                ModelViewAnchor.HMD,
                true,
                new Vector3f(0,-0.5f,-0.6f),
                new Vector3f()
        );
        appliedModelView = true;
    }

    public void startMovingByAnchor(){
        if(!isEnabled()) return;

        ModelViewAnchor posAnchor = demoModelViewOptions.getPositionAnchor();
        emulatingModelView = false;
        movingByAnchor = posAnchor == ModelViewAnchor.MAIN_HAND
                ? ModelViewAnchor.OFFHAND
                : ModelViewAnchor.MAIN_HAND;
        demoModelViewOptions.setMovingDemoAnchor(movingByAnchor);

        ClientContext.cursorHandler.changeActiveCursorHand(
                movingByAnchor == ModelViewAnchor.OFFHAND
                ? ControllerHand.OFFHAND : ControllerHand.MAIN
        );
    }

    public void stopAnchorMoving(){
        if(!isEnabled()) return;

        applyNewOffset();

        movingByAnchor = null;
        demoModelViewOptions.setMovingDemoAnchor(movingByAnchor);
    }

    public void applyNewOffset(){
        if(!isEnabled()) return;
        PoseData renderPose = ClientContext.player
                .getPose(PoseType.RENDER);
        emulatingModelView = true;

        ModelViewAnchor posAnchor = demoModelViewOptions.getPositionAnchor();
        ModelViewAnchor rotationAnchor = demoModelViewOptions.getRotationAnchor();

        PoseElement componentAnchorPos = posAnchor.getSupplier()
                .apply(renderPose)
                .getComponent();
        PoseElement componentAnchorRot = rotationAnchor.getSupplier()
                .apply(renderPose)
                .getComponent();
        Vec3 componentPos = componentAnchorPos.getPosition();
        Matrix4fc componentRotation = componentAnchorRot.getRotationMatrix();

        Vector3f offsetPos = componentAnchorPos
                .reverseCustomVector(
                        getPosition().subtract(componentPos)
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

        Vector3f rotationOffset = rotationAnchor.reverseAnchorRotation(
                componentRotation,
                getRotation()
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
    public boolean isCursorSupported() {
        return movingByAnchor != null;
    }

    @Override
    public boolean updateVisibility() {
        return true;
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if(movingByAnchor == null) return true;
        stopAnchorMoving();
        return true;
    }

    @Override
    protected @NotNull List<OverlayOptionCategory> createOptions() {
        return List.of(
        );
    }
}
