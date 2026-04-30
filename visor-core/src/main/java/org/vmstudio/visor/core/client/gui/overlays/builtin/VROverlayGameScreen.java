package org.vmstudio.visor.core.client.gui.overlays.builtin;

import com.mojang.blaze3d.platform.Window;
import org.vmstudio.visor.api.client.input.MouseButtonType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.gui.VROverlayManager;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayCursorData;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayHelper;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayFrameBuffer;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.extensions.client.WindowExtension;
import org.vmstudio.visor.core.client.utils.ClientUtils;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


public class VROverlayGameScreen extends VROverlayFrameBuffer {
    public static final String ID = "game_screen";

    private Vector3fc relativePosition = null;
    private Matrix4f relativeRotation = null;

    private float overlayScale = 1.0f;


    private boolean inMainMenu = true;
    public VROverlayGameScreen(@NotNull VisorAddon owner,
                               @NotNull String id) {
        super(
                owner,
                id,
                ComponentPriority.LOW,
                null,
                1.0f
        );
        setEnabled(true);
    }

    @Override
    public void onRender(float partialTicks) {
        renderTarget = ClientContext.renderer.guiTarget.getTarget();

    }

    @Override
    protected void onPreTick() {
        renderTarget = ClientContext.renderer.guiTarget.getTarget();

    }

    @Override
    protected boolean updateVisibility() {
        return MC.screen != null;
    }


    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    public void onScreenChanged(Screen previousGuiScreen,
                                Screen newScreen,
                                boolean releaseKeys
    ) {

        if (releaseKeys) {
            //@TODO
            //CLIENT_CONTEXT.inputManager.setIgnoreButtonsPressed(true);
        }

        VROverlayManager overlayManager = ClientContext.overlayManager;
        var keyboardAccessor = overlayManager
                .getKeyboardAccessor();
        if (newScreen == null) {
            resetOrient();
            Screen attachedTo = keyboardAccessor.getAttachedTo();
            if (attachedTo != null
                    && attachedTo == previousGuiScreen) {
                if(keyboardAccessor.isStaticAttachment()) {
                    keyboardAccessor.showKeyboard(null);
                }else {
                    keyboardAccessor.setVisible(false);
                }
            }
        } else if (newScreen instanceof ChatScreen) {
            if(!keyboardAccessor.isVisible()
                    || keyboardAccessor.getAttachedTo() != null){
                keyboardAccessor.showKeyboard( newScreen);
            }
        }

        orient(previousGuiScreen,newScreen);

    }

    private void orient(Screen previousGuiScreen,
                        Screen newScreen){
        inMainMenu = (MC.gameRenderer == null
                || willBeInMenuRoom(newScreen));
        if (inMainMenu) {
            orientMainMenu();
            return;
        }
        overlayScale = 1.0f;
        if ((previousGuiScreen == null && newScreen != null)
                || newScreen instanceof ChatScreen
                || newScreen instanceof BookEditScreen
                || newScreen instanceof AbstractSignEditScreen
                || relativePosition == null
                || relativeRotation == null) {
            VRPose hmd = ClientContext.localPlayer
                    .getPoseData(PlayerPoseType.RELATIVE)
                    .getHmd();
            Vector3f forwardVec = new Vector3f(0.0f, 0.0f, -2.0f);

            if (newScreen instanceof ChatScreen) {
                forwardVec = new Vector3f(0.0f, 0.5f, -2.0f);
            } else if (newScreen instanceof BookEditScreen
                    || newScreen instanceof AbstractSignEditScreen) {
                forwardVec = new Vector3f(0.0f, 0.25f, -2.0f);
            }

            var hmdPos = hmd.getPosition();
            var offset = hmd.getCustomVector(forwardVec);
            relativePosition = new Vector3f(
                    offset.x / 2.0f + hmdPos.x(),
                    offset.y / 2.0f + hmdPos.y(),
                    offset.z / 2.0f + hmdPos.z()
            );

            // orient screen
            Vector3f look = new Vector3f(
                    relativePosition.x() - hmdPos.x(),
                    relativePosition.y() - hmdPos.y(),
                    relativePosition.z() - hmdPos.z()
            );

            float yaw = (float) (Math.PI + Mth.atan2(look.x, look.z));
            float pitch = (float) Math.asin((look.y / look.length()));

            relativeRotation = new Matrix4f().rotationY(yaw)
                    .mul(new Matrix4f().rotationX(pitch));

        }

    }

    private void orientMainMenu(){

        ClientContext.localPlayer.setRotationY(0);
        overlayScale = 2.0f;
        Vector2f afloat = ClientUtils.getPlayAreaSize();
        relativePosition = new Vector3f(
                0.02f,
                1.3F,
                -Math.max(
                        afloat.y / 2.0F,
                        1.5F
                )
        );
        relativeRotation = new Matrix4f();

    }

    @Override
    public void onUpdatePose(float partialTicks) {

        if (relativePosition == null || relativeRotation == null) {
            orient(
                    null,
                    MC.screen
            );
        }

        VROverlayHelper.applyRelativePose(
                this,
                overlayScale,
                relativePosition,
                relativeRotation
        );

    }


    @Override
    public void updateCursorData(boolean activeCursor,
                                 float rawX, float rawY) {
        if (!isEnabled()) return;
        if(!activeCursor) return;

        boolean withinGui = rawX >= 0f && rawX <= 1f
                && rawY >= 0f && rawY <= 1f;
        boolean onDragHandle = isCursorOnDragHandle(rawX, rawY);
        boolean onResizeHandle = isCursorOnResizeHandle(rawX, rawY);
        if (!withinGui && !onDragHandle && !onResizeHandle) {
            //do nothing. If we change mouse position here
            // to emulate mouse exiting the screen, bugs appear
            // (todo find a way to emulate without bugs)
            return;
        }

        // ---- Preparing
        Window mcWindow = MC.getWindow();
        var guiManager = ClientContext.guiManager;


        int screenWidth = ((WindowExtension) (Object) mcWindow)
                .visor$getActualScreenWidth();
        int screenHeight = ((WindowExtension) (Object) mcWindow)
                .visor$getActualScreenHeight();

        int guiScaledWidth = guiManager.getGuiScaledWidth();
        int guiScaledHeight = guiManager.getGuiScaledHeight();


        VROverlayCursorData cursorData = activeCursorData;

        // ---- Updating mouse data
        cursorData.setRawCursorX(rawX);
        cursorData.setRawCursorY(rawY);

        cursorData.setCursorX((int)(rawX * (double) guiScaledWidth));
        cursorData.setCursorY((int)(rawY * (double) guiScaledHeight));

        if (!withinGui) {
            return;
        }

        //here as an input it requires NOT SCALED position
        InputHelper.setMousePos(
                (int)(rawX * (double) screenWidth),
                (int)(rawY * (double) screenHeight)
        );
    }


    public void resetOrient() {
        relativePosition = null;
        relativeRotation = null;
        overlayScale = 1.0f;
    }

    public boolean willBeInMenuRoom(Screen newScreen) {
        return MC.level == null ||
                newScreen instanceof WinScreen ||
                newScreen instanceof ReceivingLevelScreen ||
                newScreen instanceof ProgressScreen ||
                newScreen instanceof GenericDirtMessageScreen ||
                MC.getOverlay() != null;
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
        overlayScale = getPose().getScale();
    }

    @Override
    public void onStoppedResizing() {
        overlayScale = getPose().getScale();
    }

    @Override
    public boolean mouseClicked(double x, double y, int buttonType) {
        if (buttonType == 0 && isCursorOnResizeHandle(getRawMouseX(), getRawMouseY())) {
            startResizing();
            return true;
        }
        if (buttonType == 0 && isCursorOnDragHandle(getRawMouseX(), getRawMouseY())) {
            startDragging();
            return true;
        }
        InputHelper.pressMouse(MouseButtonType.fromId(buttonType));
        return true;
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
        InputHelper.releaseMouse(MouseButtonType.fromId(buttonType));
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        // we use here screen directly
        // since the scrollDelta received is already calculated
        // and not applicable to InputHelper scroll method
        if(MC.screen != null){
            return MC.screen.mouseScrolled(mouseX, mouseY, scrollDelta);
        }
        return false;
    }

    @Override
    public boolean supportsCursor() {
        return true;
    }
    @Override
    public boolean supportsDragging() {
        return !inMainMenu;
    }

    @Override
    public boolean supportsResizing() {
        return !inMainMenu;
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
