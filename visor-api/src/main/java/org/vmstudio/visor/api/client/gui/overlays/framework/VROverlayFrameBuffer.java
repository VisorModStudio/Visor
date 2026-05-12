package org.vmstudio.visor.api.client.gui.overlays.framework;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.ConfigFile;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.common.player.VRPose;

import java.io.IOException;
import java.util.*;

/**
 * {@link VROverlay} that renders
 * frame buffer from specified {@link RenderTarget}
 */
public abstract class VROverlayFrameBuffer implements VROverlay {
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


    @Getter
    protected RenderTarget renderTarget;



    protected final Map<String, OverlayOptionGroup<?>> optionsMap;

    @Getter
    private final @NotNull Collection<OverlayOptionGroup<?>> options;

    @Getter
    protected final ConfigFile optionsConfig;



    @Getter
    protected final VROverlayCursorData activeCursorData = new VROverlayCursorData();
    @Getter
    protected final VROverlayCursorData inactiveCursorData = new VROverlayCursorData();




    @Getter
    private boolean enabled = false;
    @Getter
    private boolean visible = false;

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

    public VROverlayFrameBuffer(@NotNull VisorAddon owner,
                                @NotNull String id){
        this(owner, id, ComponentPriority.NORMAL, null, 1.0f);
    }

    public VROverlayFrameBuffer(@NotNull VisorAddon owner,
                                @NotNull String id,
                                @NotNull ComponentPriority priority,
                                @Nullable RenderTarget renderTarget,
                                float overlayScale) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(id);
        Objects.requireNonNull(priority);
        if(overlayScale <=0){
            throw new RuntimeException("overlayScale cannot be less or equal '0'");
        }

        this.owner = owner;
        this.id = id.toLowerCase();
        this.renderTarget = renderTarget;
        this.priority = priority;

        this.pose = new VROverlayPose(this, overlayScale);

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


    protected void onPreRender(float partialTicks) {}

    protected void onRender(float partialTicks) {}

    protected abstract void onUpdatePose(float partialTicks);

    protected abstract boolean updateVisibility();

    protected void onFinishedDragging() {};

    protected void onFinishedResizing() {};

    protected void onEnable() {}

    protected void onDisable() {}

    /**
     * Create options for overlay.
     * If no options required, return empty list.
     *
     * <p>
     *     If method returns non-empty list, the optionsConfig is created
     * </p>
     * <p>If overlay is a {@link VROverlayTemplate}, the method has to return non-empty list</p>
     * @return options
     */
    @NotNull
    protected List<OverlayOptionGroup<?>> createOptions() {
        return List.of();
    }

    protected void initOptions(){
        for(var option : options){
            option.init();
        }
        if(optionsResizing != null) {
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
    public final void tick(){
        onPreTick();
        var preVisible = optionsVisibility == null || optionsVisibility.isVisible();
        visible = enabled && preVisible && updateVisibility() && renderTarget != null;
        onTick();
    }

    public void render(float partialTick){
        if(supportsVisibilityUpdateOnRender()) {
            var preVisible = optionsVisibility == null || optionsVisibility.isVisible();
            visible = enabled && preVisible  && updateVisibility() && renderTarget != null;
            if(!visible) return;
        }
        onPreRender(partialTick);
        onRender(partialTick);
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
    public void setEnabled(boolean flag) {
        if(flag == enabled) return;

        this.enabled = flag;
        if(enabled){
            onEnable();
        }else{
            visible = false;
            onDisable();
        }
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

        onFinishedDragging();
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

        onFinishedResizing();
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


    @Override
    public @Nullable OverlayOptionGroup<?> getOption(@NotNull String id) {
        return optionsMap.get(id);
    }

    @Override
    public boolean isBeingDragged() {
        return beingDragged;
    }
    @Override
    public boolean isBeingResized() {
        return beingResized;
    }

    @Override
    public boolean supportsCursor() {
        return false;
    }


    @Override
    public int getWidth() {
        return renderTarget != null ? renderTarget.width : 1;
    }

    @Override
    public int getHeight() {
        return renderTarget != null ? renderTarget.height : 1;
    }

    @Override
    public void updateCursorData(boolean activeCursor, float rawX, float rawY) {

    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonType) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int buttonType) {
        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {

    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int buttonType, double deltaX, double deltaY) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int keyScan, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return false;
    }
    @Override
    public boolean keyPressed(int keyCode, int keyScan, int modifiers) {
        return false;
    }



}
