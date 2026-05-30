package org.vmstudio.visor.api.client.gui.overlays.framework;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.*;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsResizing;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsVisibility;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.player.pose.PoseAnchor;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vmstudio.visor.api.common.player.VRPose;

import java.io.IOException;
import java.util.*;

/**
 * {@link VROverlay} that is rendered
 * as a minecraft {@link Screen}
 */
public abstract class VROverlayScreen extends Screen implements VROverlay {

    @Getter @NotNull
    private final String id;

    @Getter @NotNull
    private final VisorAddon owner;

    @Getter
    private final ComponentPriority priority;

    @Getter
    private final VROverlayPose pose;

    @Getter @Setter
    private @Nullable PoseAnchor forcedAnchor;


    @Getter @Setter
    private RenderTarget renderTarget;




    protected final Map<String, OverlayOptionGroup<?>> optionsMap;

    @Getter
    private final @NotNull Collection<OverlayOptionGroup<?>> options;

    @Getter
    protected final ConfigFile optionsConfig;


    @Getter
    private final VROverlayCursorData activeCursorData = new VROverlayCursorData();
    @Getter
    private final VROverlayCursorData inactiveCursorData = new VROverlayCursorData();


    @Getter
    protected int guiScaleFactor = 0;

    @Getter
    protected int cursorBoundsX = -1;
    @Getter
    protected int cursorBoundsY = -1;
    @Getter
    protected int cursorBoundsWidth = -1;
    @Getter
    protected int cursorBoundsHeight = -1;


    @Getter
    private boolean enabled = false;

    private boolean visible;

    protected boolean initAgain;


    private static long mouseDragDelay;
    private final boolean[] pressedDragMouseButtons = new boolean[3];

    private boolean beingDragged = false;
    private Vector3f dragPositionOffset = new Vector3f(0, 0, -0.3f);
    private Matrix4f dragRotationMatrix = new Matrix4f();

    private boolean beingResized = false;
    private HandType resizeHand;
    private final Vector3f resizeStartPosition = new Vector3f();
    private final Matrix4f resizeStartRotation = new Matrix4f();
    private float resizeStartHandDistance;
    private float resizeStartScale = 1f;

    protected OverlayOptionsVisibility optionsVisibility;
    protected OverlayOptionsResizing optionsResizing;

    public VROverlayScreen(@NotNull VisorAddon owner,
                           @NotNull String id) {
        this(owner, id, ComponentPriority.NORMAL,1.0f);
    }

    public VROverlayScreen(@NotNull VisorAddon owner,
                           @NotNull String id,
                           @NotNull ComponentPriority priority,
                           float overlayScale) {
        super(Component.literal(id));
        Objects.requireNonNull(owner);
        Objects.requireNonNull(id);
        Objects.requireNonNull(priority);
        if(overlayScale <=0){
            throw new RuntimeException("overlayScale cannot be less or equal '0'");
        }

        this.owner = owner;
        this.id = id.toLowerCase();
        this.priority = priority;
        this.pose = new VROverlayPose(this, overlayScale);

        this.minecraft = Minecraft.getInstance();

        optionsMap = new LinkedHashMap<>();
        List<OverlayOptionGroup<?>> preOptions = createOptions();
        preOptions.forEach(it->{
            optionsMap.put(it.getId(),it);
        });

        var requestedOptions = new ArrayList<>(optionsMap.values());
        optionsResizing = new OverlayOptionsResizing(
                this,
                (it)->{

                });
        requestedOptions.add(optionsResizing);
        options = Collections.unmodifiableCollection(requestedOptions);

        try {
            this.optionsConfig = VisorAPI.client()
                    .getGuiManager()
                    .getOverlayManager()
                    .getOverlayConfigAccessor()
                    .getConfigOrCreate(this);
            initOptions();
        } catch (IOException e) {
            throw new VRException(e);
        }


    }

    protected void onPreTick() {}

    protected void onTick() {}


    protected void onPreRender(GuiGraphics guiGraphics,
                               int mouseX, int mouseY,
                               float partialTicks) {}

    protected void onRender(GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {}

    protected abstract void onUpdatePose(float partialTicks);

    protected abstract boolean updateVisibility();

    protected void onVisibilityChanged(){}

    protected void onStoppedDragging() {}

    protected void onStoppedResizing() {}

    protected void onEnable() {}

    protected void onDisable() {}

    public int getRequestedWidth(){
        return VisorAPI.client().getGuiManager().getGuiWidth();
    }
    public int getRequestedHeight(){
        return VisorAPI.client().getGuiManager().getGuiHeight();
    }
    public final int getRequestedWidthScaled(){
        return Mth.ceil(getRequestedWidth() / (float) guiScaleFactor);
    }
    public final int getRequestedHeightScaled(){
        return Mth.ceil(getRequestedHeight() / (float) guiScaleFactor);
    }

    /**
     * Create options for overlay.
     * If no options required, return empty list.
     *
     * <p>
     *     If method returns non-empty list, the optionsConfig is created
     * </p>
     * <p>
     *     If overlay is the {@link VROverlayTemplate},
     *     the method always return non-empty list
     * </p>
     * @return options list
     */
    @NotNull
    protected List<OverlayOptionGroup<?>> createOptions() {
        return List.of();
    }

    protected void initOptions(){
        for(var option : options){
            option.init();
        }
        if(supportsResizing()) {
            var resizingScale = optionsResizing.getResizingScale();
            if (resizingScale != -1) {
                getPose().updateOnlyScale(resizingScale);
                OverlayOptionsPose poseOptions = getOption(OverlayOptionsPose.ID, OverlayOptionsPose.class);
                if (poseOptions != null) {
                    poseOptions.setScale(resizingScale);
                    poseOptions.save();
                }
            }
        }
        optionsVisibility = getOption(OverlayOptionsVisibility.ID, OverlayOptionsVisibility.class);
    }

    @Override
    public final void tick() {
        onPreTick();
        boolean oldVisible = visible;
        var preVisible = optionsVisibility == null || optionsVisibility.isVisible();
        visible = enabled && preVisible && updateVisibility();
        VisorAPI.client().getRenderer().updateOverlayTarget(
                this
        );
        //making sure there is a render target to draw on
        visible = visible && renderTarget != null;
        if(oldVisible != visible){
            onVisibilityChanged();
        }
        onTick();
    }


    @Override
    public final void render(@NotNull GuiGraphics guiGraphics,
                             int pMouseX, int pMouseY,
                             float partialTicks
    ) {
        if (initAgain) {
            init();
            initAgain = false;
        }
        if(supportsVisibilityUpdateOnRender()) {
            boolean oldVisible = visible;
            var preVisible = optionsVisibility == null || optionsVisibility.isVisible();
            visible = enabled && preVisible && updateVisibility();
            VisorAPI.client().getRenderer().updateOverlayTarget(
                    this
            );
            //making sure there is a render target to draw on
            visible = visible && renderTarget != null;
            if(oldVisible != visible){
                onVisibilityChanged();
            }
            if(!visible) return;
        }

        onPreRender(
                guiGraphics,
                pMouseX, pMouseY,
                partialTicks
        );

        super.render(guiGraphics, pMouseX, pMouseY, partialTicks);

        onRender(
                guiGraphics,
                pMouseX, pMouseY,
                partialTicks
        );
    }

    @Override
    public final void updatePose(float partialTicks) {
        if(beingResized) {
            applyResizePose();
            return;
        }
        if(forcedAnchor != null) {
            applyForcedPose();
            return;
        }
        onUpdatePose(partialTicks);
    }


    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics) {
        //empty
    }

    @Override
    public void setEnabled(boolean flag) {
        if (flag == enabled) return;
        if (flag) {
            enabled = true;
            updateSize();
            onEnable();
        } else {
            enabled = false;
            visible = false;
            var keyboardAccessor = VisorAPI.client().getGuiManager()
                    .getOverlayManager()
                    .getKeyboardAccessor();
            if (keyboardAccessor.getAttachedTo() == this) {
                if(keyboardAccessor.isStaticAttachment()) {
                    keyboardAccessor.showKeyboard(null);
                }else {
                    keyboardAccessor.setVisible(false);
                }
            }
            onDisable();
        }
    }


    public void updateSize(){
        guiScaleFactor = VisorAPI.client().getGuiManager().calculateScale(
                0,
                getRequestedWidth(),
                getRequestedHeight()
        );
        init(
                Minecraft.getInstance(),
                getRequestedWidthScaled(),
                getRequestedHeightScaled()
        );
    }

    @Override
    public void startDragging() {
        var vrClient = VisorAPI.client();

        HandType cursorHand = vrClient.getGuiManager().getCursorHandler().getCursorHand();
        PoseAnchor dragAnchor = cursorHand == HandType.MAIN
                ? PoseAnchor.MAIN_HAND
                : PoseAnchor.OFFHAND;
        VRPlayerPoseClient renderPose = vrClient.getVRLocalPlayer().getPoseData(PlayerPoseType.RENDER);
        VRPose anchorPose = dragAnchor.getSupplier().apply(renderPose);

        Vector3f dragPositionOffset = anchorPose.reverseCustomVector(
                getPose().getPosition().sub(anchorPose.getPosition(), new Vector3f())
        ).div(renderPose.getWorldScale());
        Matrix4f dragRotation = anchorPose.getRotation()
                .invert(new Matrix4f())
                .mul(getPose().getRotation(), new Matrix4f());

        this.dragPositionOffset.set(dragPositionOffset);
        this.dragRotationMatrix.set(dragRotation);
        this.beingDragged = true;
        setForcedAnchor(dragAnchor);
    }

    public void stopDragging() {
        setForcedAnchor(null);
        this.beingDragged = false;

        OverlayOptionsPose poseOptions = getOption(OverlayOptionsPose.ID, OverlayOptionsPose.class);
        if (poseOptions != null) {
            VRPlayerPoseClient renderPose = VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.RENDER);

            PoseAnchor posAnchor = poseOptions.getPositionAnchor();
            VRPose posAnchorPose = posAnchor.getSupplier().apply(renderPose);
            Vector3f offsetPos = posAnchorPose.reverseCustomVector(
                    getPose().getPosition().sub(posAnchorPose.getPosition(), new Vector3f())
            ).div(renderPose.getWorldScale());

            poseOptions.setPositionOffset(offsetPos);

            if (!poseOptions.isAimedRotation()) {
                PoseAnchor rotAnchor = poseOptions.getRotationAnchor();
                VRPose rotAnchorPose = rotAnchor.getSupplier().apply(renderPose);
                Matrix4f rotOffsetMatrix = rotAnchorPose.getRotation().invert(new Matrix4f())
                        .mul(getPose().getRotation(), new Matrix4f());
                poseOptions.setRotationOffset(rotOffsetMatrix);
            }

            poseOptions.save();
        }

        onStoppedDragging();
    }

    protected void applyForcedPose() {
        if (forcedAnchor == null) {
            return;
        }

        VRPlayerPoseClient renderPose = VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.RENDER);
        VRPose anchorPose = forcedAnchor.getSupplier().apply(renderPose);

        Vector3f positionOffset = new Vector3f(dragPositionOffset)
                .mul(renderPose.getWorldScale());
        Vector3f newPosition = anchorPose.getCustomVector(positionOffset)
                .add(anchorPose.getPosition());
        Matrix4f newRotation = new Matrix4f(anchorPose.getRotation())
                .mul(dragRotationMatrix, new Matrix4f());

        getPose().update(
                newPosition,
                newRotation,
                getPose().getScale()
        );
    }

    @Override
    public void startResizing() {
        if (!supportsResizing()) return;

        var vrClient = VisorAPI.client();
        HandType cursorHand = vrClient.getGuiManager().getCursorHandler().getCursorHand();
        HandType otherHand  = cursorHand.opposite();

        VRPlayerPoseClient renderPose = vrClient.getVRLocalPlayer().getPoseData(PlayerPoseType.RENDER);
        Vector3fc primaryPos = renderPose.getGripHand(cursorHand).getPosition();
        Vector3fc otherPos   = renderPose.getGripHand(otherHand).getPosition();

        this.resizeStartPosition.set(getPose().getPosition());
        this.resizeStartRotation.set(getPose().getRotation());
        this.resizeStartHandDistance = primaryPos.distance(otherPos);
        this.resizeStartScale = getPose().getScale();
        this.resizeHand = cursorHand;
        this.beingResized = true;
    }

    @Override
    public void stopResizing() {
        if (!beingResized) return;
        this.beingResized = false;
        this.resizeHand = null;

        optionsResizing.setResizingScale(getPose().getScale());
        optionsResizing.save();

        OverlayOptionsPose poseOptions = getOption(OverlayOptionsPose.ID, OverlayOptionsPose.class);
        if (poseOptions != null) {
            poseOptions.setScale(getPose().getScale());
            poseOptions.save();
        }
        onStoppedResizing();
    }

    protected void applyResizePose() {
        if (resizeHand == null) {
            return;
        }
        VRPlayerPoseClient renderPose = VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.RENDER);
        Vector3fc primaryPos = renderPose.getGripHand(resizeHand).getPosition();
        Vector3fc otherPos   = renderPose.getGripHand(resizeHand.opposite()).getPosition();
        float curDist = primaryPos.distance(otherPos);

        float newScale = resizeStartScale;
        if (resizeStartHandDistance > 1.0e-4f && curDist > 1.0e-4f) {
            float ratio = curDist / resizeStartHandDistance;
            newScale = Math.max(getMinScale(),
                    Math.min(getMaxScale(), resizeStartScale * ratio));
        }
        getPose().update(resizeStartPosition, resizeStartRotation, newScale);
    }


    public boolean canDragMouse(){
        return mouseDragDelay < System.currentTimeMillis();
    }
    public boolean isDragMouseButtonPressed(int buttonType){
        if (buttonType < 0 || buttonType >= pressedDragMouseButtons.length) {
            return false;
        }
        return pressedDragMouseButtons[buttonType];
    }
    public void startDragMouse(int buttonType){
        if (buttonType < 0 || buttonType >= pressedDragMouseButtons.length) {
            return;
        }
        pressedDragMouseButtons[buttonType] = true;
        mouseDragDelay = System.currentTimeMillis();
        mouseDragged(getMouseX(), getMouseY(), buttonType, 0, 0);
    }
    public void finishDragMouse(int buttonType){
        if (buttonType < 0 || buttonType >= pressedDragMouseButtons.length) {
            return;
        }
        pressedDragMouseButtons[buttonType] = false;
        for (boolean pressed : pressedDragMouseButtons) {
            if (pressed) return;
        }
        mouseDragDelay = Long.MAX_VALUE;
    }

    @Override
    public void updateCursorData(boolean activeCursor, float rawX, float rawY) {
        if (!enabled) return;
        boolean withinGui = rawX >= 0f && rawX <= 1f
                && rawY >= 0f && rawY <= 1f;
        boolean onDragHandle = isCursorOnDragHandle(rawX, rawY);
        boolean onResizeHandle = isCursorOnResizeHandle(rawX, rawY);
        if (!withinGui && !onDragHandle && !onResizeHandle) {
            return;
        }

        // ---- Preparing
        VROverlayCursorData cursorData = activeCursor ? activeCursorData : inactiveCursorData;


        int oldMouseX = cursorData.getCursorX();
        int oldMouseY = cursorData.getCursorY();

        // ---- Updating mouse data
        cursorData.setRawCursorX(rawX);
        cursorData.setRawCursorY(rawY);


        cursorData.setCursorX(
                (int) (rawX * (double) this.width)
        );
        cursorData.setCursorY(
                (int) (rawY * (double) this.height)
        );

        if(!activeCursor){
            return;
        }

        if (!withinGui || isBeingDragged() || isBeingResized()) {
            return;
        }

        // ---- Move and Drag events
        mouseMoved(cursorData.getCursorX(), cursorData.getCursorY());

        if (canDragMouse()) {
            int deltaX = cursorData.getCursorX() - oldMouseX;
            int deltaY = cursorData.getCursorY() - oldMouseY;
            for (int buttonType = 0; buttonType < pressedDragMouseButtons.length; buttonType++) {
                if (pressedDragMouseButtons[buttonType]) {
                    mouseDragged(
                            cursorData.getCursorX(), cursorData.getCursorY(),
                            buttonType,
                            deltaX, deltaY
                    );
                }
            }
        }

    }

    @Override
    public boolean isBeingDragged() {
        return beingDragged;
    }
    @Override
    public boolean isBeingResized() {
        return beingResized;
    }

    public boolean isVisible() {
        return visible && enabled;
    }

    @Override
    public @Nullable OverlayOptionGroup<?> getOption(@NotNull String id) {
        return optionsMap.get(id);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonType) {
        if (buttonType == 0 && isCursorOnResizeHandle(getRawMouseX(), getRawMouseY())) {
            startResizing();
            return true;
        }
        if (buttonType == 0 && isCursorOnDragHandle(getRawMouseX(), getRawMouseY())) {
            startDragging();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, buttonType);
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int buttonType) {
        if (buttonType == 0 && isBeingResized()) {
            stopResizing();
            return true;
        }
        if (buttonType == 0 && isBeingDragged()) {
            stopDragging();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, buttonType);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY,
                                int buttonType,
                                double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, buttonType, deltaX, deltaY);
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        return super.keyReleased(i, j, k);
    }
    @Override
    public boolean charTyped(char chr, int modifiers) {
        return super.charTyped(chr, modifiers);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
