package me.phoenixra.visor.api.client.gui.overlay.framework;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.gui.VRGuiManager;
import me.phoenixra.visor.api.client.gui.VROverlayManager;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayPose;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.api.common.addon.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class VROverlayScreen extends Screen implements VROverlay {

    @Getter @NotNull
    private final String id;

    @Getter @NotNull
    private final VisorAddon owner;

    @Getter
    private final ElementPriority priority;

    @Getter
    private final VROverlayPose pose;


    @Getter @Setter
    private RenderTarget renderTarget;


    @Getter
    private final VROverlayCursorData activeCursorData = new VROverlayCursorData();
    @Getter
    private final VROverlayCursorData inactiveCursorData = new VROverlayCursorData();

    //screen edges to consider valid for cursor
    @Getter
    protected int cursorEdgeX = -1;
    @Getter
    protected int cursorEdgeY = -1;
    @Getter
    protected int cursorEdgeWidth = -1;
    @Getter
    protected int cursorEdgeHeight = -1;


    @Getter
    private boolean enabled = false;

    private boolean visible;

    protected boolean initAgain;


    public VROverlayScreen(@NotNull VisorAddon owner,
                           @NotNull String id) {
        this(owner, id, ElementPriority.NORMAL,1.0f);
    }

    public VROverlayScreen(@NotNull VisorAddon owner,
                           @NotNull String id,
                           @NotNull ElementPriority priority,
                           float overlayScale) {
        super(Component.literal(id));
        Objects.requireNonNull(owner);
        Objects.requireNonNull(id);
        Objects.requireNonNull(priority);
        if(overlayScale <=0){
            throw new RuntimeException("overlayScale cannot be less or equal '0'");
        }

        this.owner = owner;
        this.id = id;
        this.priority = priority;
        this.pose = new VROverlayPose(overlayScale);

        this.minecraft = Minecraft.getInstance();

    }

    protected void onPreTick() {}

    protected abstract void onTick();


    protected void onPreRender(GuiGraphics guiGraphics,
                               int mouseX, int mouseY,
                               float partialTicks) {}

    protected void onRender(GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {}


    protected abstract boolean updateVisibility();

    protected abstract void onEnable();

    protected abstract void onDisable();


    @Override
    public final void tick() {
        onPreTick();

        boolean withinRange = false;
        if(enabled){
            withinRange = isInViewDistance();
        }
        visible = enabled && withinRange && updateVisibility();
        VisorAPI.client().getRenderer().updateOverlayTarget(
                this
        );
        //making sure there is a render target to draw on
        visible = visible && renderTarget != null;

        onTick();
    }


    @Override
    public final void render(GuiGraphics guiGraphics,
                             int pMouseX, int pMouseY,
                             float partialTicks
    ) {
        if (initAgain) {
            init();
            initAgain = false;
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
    public void renderBackground(@NotNull GuiGraphics guiGraphics) {
        //empty
    }

    @Override
    public void setEnabled(boolean flag) {
        if (flag == enabled) return;
        VRGuiManager guiManager = VisorAPI.client().getGuiManager();

        if (flag) {
            enabled = true;
            init(
                    Minecraft.getInstance(),
                    guiManager.getScaledGuiWidth(),
                    guiManager.getScaledGuiHeight()
            );
            onEnable();
        } else {
            enabled = false;
            VROverlayManager overlayHandler = VisorAPI.client()
                    .getGuiManager()
                    .getOverlayManager();
            if (overlayHandler.getKeyboardAttachedTo() == this) {
                overlayHandler.showKeyboard(false);
            }
            onDisable();
        }
    }


    @Override
    public void updateCursorData(boolean activeCursor, float rawX, float rawY) {
        if (!enabled) return;
        if (rawX < 0f || rawX > 1f
                || rawY < 0f || rawY > 1f) {
            VROverlayCursorData cursorData = activeCursor ? activeCursorData : inactiveCursorData;

            cursorData.rawCursorX = -1;
            cursorData.rawCursorY = -1;

            cursorData.cursorX = 0;
            cursorData.cursorY = 0;
            mouseMoved(cursorData.cursorX, cursorData.cursorY);
            return;
        }

        // ---- Preparing
        VRGuiManager guiManager = VisorAPI.client().getGuiManager();

        VROverlayCursorData cursorData = activeCursor ? activeCursorData : inactiveCursorData;


        int oldMouseX = cursorData.cursorX;
        int oldMouseY = cursorData.cursorY;

        // ---- Updating mouse data
        cursorData.rawCursorX = rawX;
        cursorData.rawCursorY = rawY;

        float preMouseX = (float) (
                (int) (rawX * guiManager.getScaledGuiWidth())
        );
        float preMouseY = (float) (
                (int) (rawY * guiManager.getScaledGuiHeight())
        );

        cursorData.cursorX = (int) (preMouseX * (double) this.width / (double) guiManager.getScaledGuiWidth());
        cursorData.cursorY = (int) (preMouseY * (double) this.height / (double) guiManager.getScaledGuiHeight());

        // ---- Move and Drag events
        mouseMoved(cursorData.cursorX, cursorData.cursorY);

        if (InputHelper.canDragMouse()) {
            for (int button : new int[]{0, 1, 2}) {
                if (!InputHelper.isMousePressed(button)) continue;
                int deltaX = cursorData.cursorX - oldMouseX;
                int deltaY = cursorData.cursorY - oldMouseY;
                mouseDragged(
                        cursorData.cursorX, cursorData.cursorY,
                        button,
                        deltaX, deltaY
                );
                break;
            }
        }

    }

    @Override
    public boolean isCursorWithinBounds(boolean activeCursor,
                                        float rawX, float rawY) {
        if (rawX < 0f || rawX > 1f
                || rawY < 0f || rawY > 1f) return false;

        if (cursorEdgeX == -1 || cursorEdgeY == -1
                || cursorEdgeWidth == -1 || cursorEdgeHeight == -1) return true;

        VROverlayCursorData cursorData = activeCursor ? activeCursorData : inactiveCursorData;

        float originRawMouseX = cursorData.rawCursorX;
        float originRawMouseY = cursorData.rawCursorY;
        int originMouseX = cursorData.cursorX;
        int originMouseY = cursorData.cursorY;

        updateCursorData(activeCursor, rawX, rawY);
        boolean result = cursorData.cursorX >= cursorEdgeX
                && cursorData.cursorY >= cursorEdgeY
                && cursorData.cursorX <= cursorEdgeWidth + cursorEdgeX
                && cursorData.cursorY <= cursorEdgeHeight + cursorEdgeY;

        cursorData.rawCursorX = originRawMouseX;
        cursorData.rawCursorY = originRawMouseY;
        cursorData.cursorX = originMouseX;
        cursorData.cursorY = originMouseY;
        return result;
    }



    public boolean isVisible() {
        return visible && enabled;
    }



    @Override
    public boolean mouseClicked(double x, double y, int buttonType) {
        return super.mouseClicked(x, y, buttonType);
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int buttonType) {
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


}
