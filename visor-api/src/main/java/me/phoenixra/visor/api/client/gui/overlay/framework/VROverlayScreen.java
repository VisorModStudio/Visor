package me.phoenixra.visor.api.client.gui.overlay.framework;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.gui.VRGuiManager;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayCursorData;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayPose;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
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

    protected void onTick() {};


    protected void onPreRender(GuiGraphics guiGraphics,
                               int mouseX, int mouseY,
                               float partialTicks) {}

    protected void onRender(GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {}


    protected abstract boolean updateVisibility();

    protected void onEnable() {};

    protected void onDisable() {};


    @Override
    public final void tick() {
        onPreTick();

        visible = enabled && updateVisibility();
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
            visible = false;
            var keyboardAccessor = VisorAPI.client().getGuiManager()
                    .getOverlayManager()
                    .getKeyboardAccessor();
            if (keyboardAccessor.getAttachedTo() == this) {
                keyboardAccessor.setVisible(false);
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

            cursorData.setRawCursorX(-1);
            cursorData.setRawCursorY(-1);

            cursorData.setCursorX(0);
            cursorData.setCursorY(0);
            mouseMoved(cursorData.getCursorX(), cursorData.getCursorY());
            return;
        }

        // ---- Preparing
        VRGuiManager guiManager = VisorAPI.client().getGuiManager();

        VROverlayCursorData cursorData = activeCursor ? activeCursorData : inactiveCursorData;


        int oldMouseX = cursorData.getCursorX();
        int oldMouseY = cursorData.getCursorY();

        // ---- Updating mouse data
        cursorData.setRawCursorX(rawX);
        cursorData.setRawCursorY(rawY);

        float preMouseX = (float) (
                (int) (rawX * guiManager.getScaledGuiWidth())
        );
        float preMouseY = (float) (
                (int) (rawY * guiManager.getScaledGuiHeight())
        );

        cursorData.setCursorX(
                (int) (preMouseX * (double) this.width / (double) guiManager.getScaledGuiWidth())
        );
        cursorData.setCursorY(
                (int) (preMouseY * (double) this.height / (double) guiManager.getScaledGuiHeight())
        );

        // ---- Move and Drag events
        mouseMoved(cursorData.getCursorX(), cursorData.getCursorY());

        if (InputHelper.canDragMouse()) {
            for (int button : new int[]{0, 1, 2}) {
                if (!InputHelper.isMousePressed(button)) continue;
                int deltaX = cursorData.getCursorX() - oldMouseX;
                int deltaY = cursorData.getCursorY() - oldMouseY;
                mouseDragged(
                        cursorData.getCursorX(), cursorData.getCursorY(),
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

        float originRawCursorX = cursorData.getRawCursorX();
        float originRawCursorY = cursorData.getRawCursorY();
        int originCursorX = cursorData.getCursorX();
        int originCursorY = cursorData.getCursorY();

        updateCursorData(activeCursor, rawX, rawY);
        boolean result = cursorData.getCursorX() >= cursorEdgeX
                && cursorData.getCursorY() >= cursorEdgeY
                && cursorData.getCursorX() <= cursorEdgeWidth + cursorEdgeX
                && cursorData.getCursorY() <= cursorEdgeHeight + cursorEdgeY;

        cursorData.setRawCursorX(originRawCursorX);
        cursorData.setRawCursorY(originRawCursorY);
        cursorData.setCursorX(originCursorX);
        cursorData.setCursorY(originCursorY);
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
