package org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard;

import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.events.gui.KeyboardStateChangedVREvent;
import org.vmstudio.visor.api.client.gui.VRKeyboardAccessor;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayHelper;
import org.vmstudio.visor.api.client.gui.overlays.framework.screen.VROverlayScreenInScreen;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.player.pose.PoseAnchor;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.screens.VRKeyboardScreen;
import org.vmstudio.visor.core.client.settings.VRClientSettings;

import java.util.List;

public class VROverlayKeyboard extends VROverlayScreenInScreen<VRKeyboardScreen>
        implements VRKeyboardAccessor, VREventListener {
    public static final String ID = "keyboard";

    private final Vector3f posOffset = new Vector3f(0,-0.5f,-0.6f);
    private final Vector3f rotationOffset = new Vector3f(0,0,0);

    private Vector3fc relativePosition = null;
    private Matrix4f relativeRotation = null;

    @Getter
    private boolean shiftPressed = false;

    @Getter
    private KeyboardLayout activeLayout = KeyboardLayout.ENGLISH;

    @Getter
    @Nullable
    private Screen attachedTo;

    @Getter
    private boolean staticAttachment;

    @Getter
    private boolean shown;

    public VROverlayKeyboard(@NotNull VisorAddon owner,
                             @NotNull String id) {
        super(owner, id, ComponentPriority.HIGHER,0.5f,
                new VRKeyboardScreen(Component.literal(""))
        );
        getScreen().setOverlayKeyboard(this);
        setEnabled(true);

        ClientContext.overlayManager.setKeyboardAccessor(this);

        VisorAPI.eventBus().registerListener(owner,this);
    }

    @Override
    protected void init() {
        super.init();
        cursorBoundsX = getScreen().getCursorBoundsX();
        cursorBoundsY = getScreen().getCursorBoundsY();
        cursorBoundsWidth = getScreen().getCursorBoundsWidth();
        cursorBoundsHeight = getScreen().getCursorBoundsHeight();
    }


    @Override
    protected void onPreRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if(ClientContext.cursorHandler.getFocusedOverlayScreen() != this){
            getScreen().clearPress();
        }
    }

    @Override
    public void resetPose(){
        VROverlayHelper.applyPose(
                this,
                PoseAnchor.HMD,
                PoseAnchor.HMD,
                getPose().getScale(),
                true,
                posOffset,
                rotationOffset
        );
        relativePosition = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RELATIVE)
                .convertPositionFrom(PlayerPoseType.RENDER, getPose().getPosition());
        relativeRotation = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RELATIVE)
                .convertRotationFrom(PlayerPoseType.RENDER, getPose().getRotation());
    }

    @Override
    public void setVisible(boolean flag){
        setVisible(flag, null);
    }

    @Override
    public void showKeyboard(@Nullable Screen attachTo) {
        setVisible(true, attachTo);
    }

    @Override
    public boolean updateVisibility() {
        return shown;
    }

    @Override
    public void onUpdatePose(float partialTicks) {
        VROverlayHelper.applyRelativePose(
                this,
                getPose().getScale(),
                relativePosition,
                relativeRotation
        );
    }

    @Override
    public void onStoppedDragging() {
        var relativePose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RELATIVE);
        relativePosition = relativePose.convertPositionFrom(
                PlayerPoseType.RENDER,
                getPose().getPosition()
        );
        relativeRotation = relativePose.convertRotationFrom(
                PlayerPoseType.RENDER,
                getPose().getRotation()
        );
    }

    @Override
    public boolean supportsTwoCursors() {
        return true;
    }

    @Override
    public boolean supportsDragging() {
        return true;
    }

    @Override
    public boolean supportsResizing() {
        return true;
    }

    public void setVisible(boolean newVisibility,
                           @Nullable Screen attachedTo) {
        boolean changedVisibility = newVisibility != shown;

        if(!changedVisibility && attachedTo == this.attachedTo){
            return; //nothing changed
        }

        var event = new KeyboardStateChangedVREvent(
                this, newVisibility, attachedTo
        );
        VisorAPI.eventBus().callEvent(
                event
        );
        if(event.isCanceled()){
            return;
        }

        boolean prevShown = shown;
        shown = newVisibility;

        if (shown) {
            orient(attachedTo, changedVisibility);
            shiftPressed = false;
            activeLayout = getEnabledLayoutIds().get(0);
            initAgain = true;
            if(!prevShown && attachedTo == null){
                staticAttachment = true;
            }
        } else {
            getScreen().clearPress();
            staticAttachment = false;
            this.attachedTo = null;
        }
    }


    public void setShiftPressed(boolean shift) {
        if (shift != this.shiftPressed) {
            this.shiftPressed = shift;
            this.initAgain = true;
        }
    }

    public void cycleLayout() {
        List<KeyboardLayout> enabledLayouts = getEnabledLayoutIds();
        int currentIndex = enabledLayouts.indexOf(activeLayout);
        if (currentIndex < 0) {
            setActiveLayout(enabledLayouts.get(0));
            return;
        }
        setActiveLayout(
                enabledLayouts.get((currentIndex + 1) % enabledLayouts.size())
        );
    }

    public void setActiveLayout(@NotNull KeyboardLayout activeLayout) {
        if (this.activeLayout != activeLayout) {
            this.activeLayout = activeLayout;
            this.initAgain = true;
        }
    }

    public @NotNull List<KeyboardLayout> getEnabledLayoutIds() {
        List<KeyboardLayout> enabledLayouts = VRClientSettings.getEffectiveKeyboardLayouts();
        if (enabledLayouts.isEmpty()) {
            return List.of(KeyboardLayout.ENGLISH);
        }
        return enabledLayouts;
    }

    public boolean hasMultipleLayouts() {
        return getEnabledLayoutIds().size() > 1;
    }

    private void orient(@Nullable Screen attachedTo, boolean changePose) {
        if (!shown) {
            this.attachedTo = null;
            return;
        }

        this.attachedTo = attachedTo;

        if(changePose) {
            VROverlayHelper.applyPose(
                    this,
                    PoseAnchor.HMD,
                    PoseAnchor.HMD,
                    getPose().getScale(),
                    true,
                    posOffset,
                    rotationOffset
            );
            relativePosition = ClientContext.localPlayer.getPoseData(PlayerPoseType.RELATIVE)
                    .convertPositionFrom(PlayerPoseType.RENDER, getPose().getPosition());
            relativeRotation = ClientContext.localPlayer.getPoseData(PlayerPoseType.RELATIVE)
                    .convertRotationFrom(PlayerPoseType.RENDER, getPose().getRotation());
        }
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
