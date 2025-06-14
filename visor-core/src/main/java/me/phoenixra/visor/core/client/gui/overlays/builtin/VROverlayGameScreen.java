package me.phoenixra.visor.core.client.gui.overlays.builtin;

import com.mojang.blaze3d.platform.Window;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.gui.GuiManager;
import me.phoenixra.visor.api.client.gui.OverlayManager;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.framework.OverlayCursorData;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayFrameBuffer;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.keyboard.VROverlayKeyboard;
import me.phoenixra.visor.core.client.mcmodified.WindowModified;
import me.phoenixra.visor.core.client.utils.ClientUtils;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


public class VROverlayGameScreen extends VROverlayFrameBuffer {
    public static final String ID = "game_screen";

    private Vec3 roomPosition = null;
    private Matrix4f roomRotation = null;


    public VROverlayGameScreen(@NotNull VisorAddon owner,
                               @NotNull String id) {
        super(owner, id, null);
        setEnabled(true);
    }

    @Override
    public void onRender(float partialTicks) {
        renderTarget = ClientContext.renderer.guiTarget.getTarget();

    }

    @Override
    protected void onTick() {
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

        OverlayManager overlayManager = ClientContext.overlayManager;
        if (newScreen == null) {
            resetOrient();
            Screen attachedTo = overlayManager.getKeyboardAttachedTo();
            if (attachedTo != null
                    && attachedTo == previousGuiScreen) {
                overlayManager.showKeyboard(false);
            }
        } else if (newScreen instanceof ChatScreen) {
            if(!overlayManager.isShowingKeyboard()
                    || overlayManager.getKeyboardAttachedTo() != null){
                overlayManager.showKeyboard(true, newScreen);
            }
        }

        orient(previousGuiScreen,newScreen);

    }

    private void orient(Screen previousGuiScreen,
                               Screen newScreen){
        boolean mainMenu = (MC.gameRenderer == null
                || willBeInMenuRoom(newScreen));
        if (mainMenu) {
            orientMainMenu();
            return;
        }
        setOverlayScale(1.0F);
        if ((previousGuiScreen == null && newScreen != null)
                || newScreen instanceof ChatScreen
                || newScreen instanceof BookEditScreen
                || newScreen instanceof AbstractSignEditScreen) {
            PoseElement hmd = ClientContext.player
                    .getPose(PoseType.ROOM)
                    .getHmd();
            Vector3f forwardVec = new Vector3f(0.0f, 0.0f, -2.0f);

            if (newScreen instanceof ChatScreen) {
                forwardVec = new Vector3f(0.0f, 0.5f, -2.0f);
            } else if (newScreen instanceof BookEditScreen
                    || newScreen instanceof AbstractSignEditScreen) {
                forwardVec = new Vector3f(0.0f, 0.25f, -2.0f);
            }

            Vec3 hmdPos = hmd.getPosition();
            Vec3 offset = hmd.getCustomVector(forwardVec);
            roomPosition = new Vec3(
                    offset.x / 2.0D + hmdPos.x,
                    offset.y / 2.0D + hmdPos.y,
                    offset.z / 2.0D + hmdPos.z
            );

            // orient screen
            Vector3f look = new Vector3f(
                    (float) (roomPosition.x - hmdPos.x),
                    (float) (roomPosition.y - hmdPos.y),
                    (float) (roomPosition.z - hmdPos.z)
            );

            float yaw = (float) (Math.PI + Mth.atan2(look.x, look.z));
            float pitch = (float) Math.asin((look.y / look.length()));

            roomRotation = new Matrix4f().rotationY(yaw)
                    .mul(new Matrix4f().rotationX(pitch));

        }
        VROverlayKeyboard keyboard = ClientContext.overlayManager.getKeyboardOverlay();

        keyboard.updateOrient();
    }

    private void orientMainMenu(){

        ClientContext.player.setRotationY(0);
        setOverlayScale(2.0F);
        Vector2f afloat = ClientUtils.getPlayAreaSize();
        roomPosition = new Vec3(
                0.02D,
                1.3F,
                -Math.max(
                        afloat != null
                                ? afloat.y / 2.0F
                                : 0.0F,
                        1.5F
                )
        );
        roomRotation = new Matrix4f();

    }

    @Override
    public void applyModelView(float partialTick) {
        //check state and update if needed
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
        if (roomPosition == null) {
            orient(
                    null,
                    MC.screen
            );
            return;
        }

        PoseData renderPose = ClientContext.player
                .getPose(PoseType.RENDER);

        Vec3 renderScreenPos = renderPose.convertPosition(
                PoseType.ROOM,
                roomPosition
        );
        Matrix4f renderScreenRotation =  renderPose.convertRotation(
                PoseType.ROOM,
                roomRotation
        );

        //applying

        setPosition(renderScreenPos);
        setRotation(renderScreenRotation);

    }


    @Override
    public void updateMousePosition(boolean activeCursorHand,
                                    float rawX, float rawY) {
        if (!isEnabled()) return;
        if(rawX == -1 && rawY == -1){
            OverlayCursorData cursorData = activeCursorHand ? activeCursorData : inactiveCursorData;

            cursorData.rawCursorX = 0;
            cursorData.rawCursorY = 0;
            cursorData.cursorInGuiX = 0;
            cursorData.cursorInGuiY = 0;

            cursorData.mouseX = 0;
            cursorData.mouseY = 0;
            if(activeCursorHand) {
                InputHelper.setMousePos(
                        cursorData.mouseX,
                        cursorData.mouseY
                );
            }
            return;
        }

        GuiManager guiManager = ClientContext.guiManager;
        Window mcWindow = MC.getWindow();
        float cursorInGuiX;
        float cursorInGuiY;
        if (rawX >= 0.0F && rawY >= 0.0F
                && rawX <= 1.0F && rawY <= 1.0F) {
            cursorInGuiX = (float) (
                    (int) (rawX * guiManager.getScaledGuiWidth())
            );
            cursorInGuiY = (float) (
                    (int) (rawY * guiManager.getScaledGuiHeight())
            );
        } else {
            cursorInGuiX = (float) (
                    (int) (Math.min(1.0f, Math.max(rawX, 0.0f)) * guiManager.getScaledGuiWidth())
            );
            cursorInGuiY = (float) (
                    (int) (Math.min(1.0f, Math.max(rawY, 0.0f)) * guiManager.getScaledGuiHeight())
            );
        }

        OverlayCursorData cursorData = activeCursorHand ? activeCursorData : inactiveCursorData;

        cursorData.rawCursorX = rawX;
        cursorData.rawCursorY = rawY;
        cursorData.cursorInGuiX = cursorInGuiX;
        cursorData.cursorInGuiY = cursorInGuiY;

        if(!activeCursorHand){
            return;
        }
        int width = ((WindowModified) (Object) mcWindow)
                .visor$getActualWidth();
        int height = ((WindowModified) (Object) mcWindow)
                .visor$getActualHeight();
        cursorData.mouseX = (int)(cursorData.cursorInGuiX * (double) width / (double) guiManager.getScaledGuiWidth());
        cursorData.mouseY = (int)(cursorData.cursorInGuiY * (double) height / (double) guiManager.getScaledGuiHeight());
        InputHelper.setMousePos(
                cursorData.mouseX,
                cursorData.mouseY
        );

    }


    public void resetOrient() {
        roomPosition = null;
        roomRotation = null;
        setOverlayScale(1.0f);
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
    public boolean mouseClicked(double d, double e, int i) {
        InputHelper.pressMouse(i);
        return true;
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        InputHelper.releaseMouse(i);
        return true;
    }



    @Override
    public boolean isCursorSupported() {
        return true;
    }

    @Override
    protected @NotNull List<OverlayOptionCategory> createOptions() {
        return List.of();
    }
}
