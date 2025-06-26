package me.phoenixra.visor.core.client.gui.overlays.builtin;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.gui.VRGuiManager;
import me.phoenixra.visor.api.client.gui.VROverlayManager;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayCursorData;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayFrameBuffer;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.mcmodified.WindowModified;
import me.phoenixra.visor.core.client.utils.ClientUtils;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


public class VROverlayGameScreen extends VROverlayFrameBuffer {
    public static final String ID = "game_screen";

    private Vector3fc roomPosition = null;
    private Matrix4f roomRotation = null;

    private float overlayScale = 1.0f;

    public VROverlayGameScreen(@NotNull VisorAddon owner,
                               @NotNull String id) {
        super(
                owner,
                id,
                ElementPriority.LOW,
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

        if (MC.screen != null
                && roomPosition == null) {
            //mods/addons did something
            onScreenChanged(
                    null, MC.screen,
                    false
            );
        } else if (MC.screen == null
                && roomPosition != null) {
            //mods/addons did something
            onScreenChanged(
                    null,
                    null,
                    false
            );
        }

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
                keyboardAccessor.setVisible(false);
            }
        } else if (newScreen instanceof ChatScreen) {
            if(!keyboardAccessor.isVisible()
                    || keyboardAccessor.getAttachedTo() != null){
                keyboardAccessor.showKeyboard( newScreen);
            }
        }

        orient(previousGuiScreen,newScreen);

        updatePose(1);

    }

    private void orient(Screen previousGuiScreen,
                               Screen newScreen){
        boolean mainMenu = (MC.gameRenderer == null
                || willBeInMenuRoom(newScreen));
        if (mainMenu) {
            orientMainMenu();
            return;
        }
        overlayScale = 1.0f;
        if ((previousGuiScreen == null && newScreen != null)
                || newScreen instanceof ChatScreen
                || newScreen instanceof BookEditScreen
                || newScreen instanceof AbstractSignEditScreen) {
            PoseElement hmd = ClientContext.player
                    .getPose(PoseDataType.ROOM)
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
            roomPosition = new Vector3f(
                    offset.x / 2.0f + hmdPos.x(),
                    offset.y / 2.0f + hmdPos.y(),
                    offset.z / 2.0f + hmdPos.z()
            );

            // orient screen
            Vector3f look = new Vector3f(
                    roomPosition.x() - hmdPos.x(),
                    roomPosition.y() - hmdPos.y(),
                    roomPosition.z() - hmdPos.z()
            );

            float yaw = (float) (Math.PI + Mth.atan2(look.x, look.z));
            float pitch = (float) Math.asin((look.y / look.length()));

            roomRotation = new Matrix4f().rotationY(yaw)
                    .mul(new Matrix4f().rotationX(pitch));

        }

        ClientContext.overlayManager.getKeyboardAccessor()
                .resetPose();
    }

    private void orientMainMenu(){

        ClientContext.player.setRotationY(0);
        overlayScale = 2.0f;
        Vector2f afloat = ClientUtils.getPlayAreaSize();
        roomPosition = new Vector3f(
                0.02f,
                1.3F,
                -Math.max(
                        afloat.y / 2.0F,
                        1.5F
                )
        );
        roomRotation = new Matrix4f();

    }

    @Override
    public void updatePose(float partialTicks) {

        if (roomPosition == null || roomRotation == null) {
            orient(
                    null,
                    MC.screen
            );
            return;
        }
        PoseData renderPose = ClientContext.player
                .getPose(PoseDataType.RENDER);

        Vector3f renderScreenPos = renderPose.convertPosition(
                PoseDataType.ROOM,
                roomPosition
        );
        Matrix4f renderScreenRotation =  renderPose.convertRotation(
                PoseDataType.ROOM,
                roomRotation
        );

        //applying

        getPose().update(
                renderScreenPos,
                renderScreenRotation,
                overlayScale
        );

    }


    @Override
    public void updateCursorData(boolean activeCursor,
                                 float rawX, float rawY) {
        if (!isEnabled()) return;
        if(!activeCursor) return;

        if (rawX < 0f || rawX > 1f
                || rawY < 0f || rawY > 1f) {
            VROverlayCursorData cursorData = activeCursorData;

            cursorData.setRawCursorX(-1);
            cursorData.setRawCursorY(-1);
            cursorData.setCursorX(0);
            cursorData.setCursorY(0);

            InputHelper.setMousePos(
                    0,
                    0
            );
            return;
        }

        // ---- Preparing
        VRGuiManager guiManager = VisorAPI.client().getGuiManager();

        var mcWindow = ((WindowModified) (Object) MC.getWindow());
        int windowWidth = mcWindow.visor$getActualWidth();
        int windowHeight = mcWindow.visor$getActualHeight();

        VROverlayCursorData cursorData = activeCursorData;

        // ---- Updating mouse data
        cursorData.setRawCursorX(rawX);
        cursorData.setRawCursorY(rawY);

        float preMouseX = (float) (
                (int) (rawX * guiManager.getScaledGuiWidth())
        );
        float preMouseY = (float) (
                (int) (rawY * guiManager.getScaledGuiHeight())
        );


        cursorData.setCursorX((int)(preMouseX * (double) windowWidth / (double) guiManager.getScaledGuiWidth()));
        cursorData.setCursorY((int)(preMouseY * (double) windowHeight / (double) guiManager.getScaledGuiHeight()));
        InputHelper.setMousePos(
                cursorData.getCursorX(),
                cursorData.getCursorY()
        );

    }


    public void resetOrient() {
        roomPosition = null;
        roomRotation = null;
        overlayScale = 1.0f;
    }

    public boolean  willBeInMenuRoom(Screen newScreen) {
        return MC.level == null ||
                newScreen instanceof WinScreen ||
                newScreen instanceof ReceivingLevelScreen ||
                newScreen instanceof ProgressScreen ||
                newScreen instanceof GenericDirtMessageScreen ||
                MC.getOverlay() != null;
    }

    @Override
    public boolean mouseClicked(double x, double y, int buttonType) {
        InputHelper.pressMouse(buttonType);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int buttonType) {
        InputHelper.releaseMouse(buttonType);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        InputHelper.scrollMouse(0, scrollDelta);
        return true;
    }

    @Override
    public boolean supportsCursor() {
        return true;
    }

}
