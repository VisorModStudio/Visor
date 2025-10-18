package me.phoenixra.visor.core.client.gui.overlays.builtin.keyboard;


import lombok.Getter;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.events.AllowClientFeatureVREvent;
import me.phoenixra.visor.api.client.gui.VRKeyboardAccessor;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlays.framework.screen.VROverlayScreenInScreen;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.eventbus.listener.VREventHandler;
import me.phoenixra.visor.api.common.eventbus.listener.VREventListener;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.screens.VRKeyboardScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;


public class VROverlayKeyboard extends VROverlayScreenInScreen<VRKeyboardScreen>
        implements VRKeyboardAccessor, VREventListener {
    public static final String ID = "keyboard";

    private final Vector3f posOffset = new Vector3f(0,-0.5f,-0.6f);
    private final Vector3f rotationOffset = new Vector3f(0,0,0);

    private Vector3fc roomPosition = null;
    private Matrix4f roomRotation = null;

    @Getter
    private boolean shiftPressed = false;

    @Getter
    @Nullable
    private Screen attachedTo;

    @Getter
    private boolean shown;

    public VROverlayKeyboard(@NotNull VisorAddon owner,
                             @NotNull String id) {
        super(owner, id, ElementPriority.HIGHER,0.5f,
                new VRKeyboardScreen(Component.literal(""))
        );
        getScreen().setOverlayKeyboard(this);
        setEnabled(true);

        ClientContext.overlayManager.setKeyboardAccessor(this);

        VisorAPI.eventBus().registerListener(owner,this);
    }


    @VREventHandler
    public void disableWorldHands(AllowClientFeatureVREvent event){
        if(event.getFeature() == ClientFeature.VR_WORLD_HANDS
                || event.getFeature() == ClientFeature.AIM_EFFECTS
                || event.getFeature() == ClientFeature.INPUT_MOVEMENT) {
            if(isVisible()){
                event.setCanceled(true);
            }
        }
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
        roomPosition = ClientContext.player.getPoseData(PoseDataType.ROOM)
                .convertPositionFrom(PoseDataType.RENDER, getPose().getPosition());
        roomRotation = ClientContext.player.getPoseData(PoseDataType.ROOM)
                .convertRotationFrom(PoseDataType.RENDER, getPose().getRotation());
    }

    @Override
    public void setVisible(boolean flag){
        setVisible(flag, null);
    }

    @Override
    public void showKeyboard(@NotNull Screen attachTo) {
        setVisible(true, attachTo);
    }

    @Override
    public boolean updateVisibility() {
        return shown;
    }


    @Override
    public void onUpdatePose(float partialTicks) {
        VROverlayHelper.applyRoomPose(
                this,
                getPose().getScale(),
                roomPosition,
                roomRotation
        );
    }



    @Override
    public boolean supportsTwoCursors() {
        return true;
    }


    public void setVisible(boolean flag,
                           @Nullable Screen attachedTo) {
        shown = flag;

        if (shown) {
            orient(attachedTo);
            shiftPressed = false;
            initAgain = true;
        } else {
            getScreen().clearPress();
            this.attachedTo = null;
        }
    }


    public void setShiftPressed(boolean shift) {
        if (shift != this.shiftPressed) {
            this.shiftPressed = shift;
            this.initAgain = true;
        }
    }

    private void orient(@Nullable Screen attachedTo) {
        if (!shown) {
            this.attachedTo = null;
            return;
        }

        this.attachedTo = attachedTo;

        VROverlayHelper.applyPose(
                this,
                PoseAnchor.HMD,
                PoseAnchor.HMD,
                getPose().getScale(),
                true,
                posOffset,
                rotationOffset
        );
        roomPosition = ClientContext.player.getPoseData(PoseDataType.ROOM)
                .convertPositionFrom(PoseDataType.RENDER, getPose().getPosition());
        roomRotation = ClientContext.player.getPoseData(PoseDataType.ROOM)
                .convertRotationFrom(PoseDataType.RENDER, getPose().getRotation());
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
