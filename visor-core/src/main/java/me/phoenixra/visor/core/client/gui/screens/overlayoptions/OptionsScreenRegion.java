package me.phoenixra.visor.core.client.gui.screens.overlayoptions;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.visor.api.client.gui.overlays.options.OptionTextures;
import me.phoenixra.visor.api.client.gui.overlays.options.OptionsScreen;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsScreenRegion;
import me.phoenixra.visor.api.client.gui.overlays.options.types.properties.PropertyInt;
import me.phoenixra.visor.api.client.gui.widgets.EditBoxImage;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class OptionsScreenRegion extends OptionsScreen<OverlayOptionsScreenRegion> {
    private static final int FIELD_HEIGHT = 15;
    private static final int ROW2_Y = 60;

    private static final int PREVIEW_MARGIN = 8; // margin inside background

    private static final int KNOB_SIZE = 8; // visual size in pixels
    private static final int KNOB_HALF = KNOB_SIZE / 2;

    private enum DragHandle {
        NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }


    private PropertyInt propertyRegionX;
    private PropertyInt propertyRegionY;

    private PropertyInt propertyRegionWidth;
    private PropertyInt propertyRegionHeight;

    // Preview layout state
    private int previewX;
    private int previewY;
    private int previewW;
    private int previewH;
    private double previewScale;

    // Dragging state

    private DragHandle activeHandle = DragHandle.NONE;
    private int dragStartMouseX;
    private int dragStartMouseY;
    private int startRegionX;
    private int startRegionY;
    private int startRegionW;
    private int startRegionH;


    private EditBoxImage widgetRegionX;
    private EditBoxImage widgetRegionY;
    private EditBoxImage widgetRegionWidth;
    private EditBoxImage widgetRegionHeight;
    public OptionsScreenRegion(@NotNull OverlayOptionsScreenRegion optionCategory) {
        super(optionCategory, Background.VERTICAL_WIDER);
    }

    @Override
    protected void onInit() {
        // Row 1: X (left), Y (right)
        this.propertyRegionX = new PropertyInt(
                "regionX",
                optionCategory.getRegionX(),
                0,
                optionCategory.getScreenWidth(),
                new WidgetInfoEditBox()
                        .pos(cursorBoundsX+15, cursorBoundsY+30)
                        .size((cursorBoundsWidth - 30)/2 - 2, 15)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("visor.overlay.options.screen_region.x")))
        );
        this.propertyRegionY = new PropertyInt(
                "regionY",
                optionCategory.getRegionY(),
                0,
                optionCategory.getScreenHeight(),
                new WidgetInfoEditBox()
                        .pos((cursorBoundsX+15) + (cursorBoundsWidth - 30)/2 + 2, cursorBoundsY+30)
                        .size((cursorBoundsWidth - 30)/2 - 4, 15)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("visor.overlay.options.screen_region.y")))
        );

        // Row 2: Width (left), Height (right)
        this.propertyRegionWidth = new PropertyInt(
                "regionWidth",
                optionCategory.getRegionWidth(),
                1,
                optionCategory.getScreenWidth(),
                new WidgetInfoEditBox()
                        .pos(cursorBoundsX+15, cursorBoundsY+45 + 4)
                        .size((cursorBoundsWidth - 30)/2 - 2, 15)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("visor.overlay.options.screen_region.width")))
        );
        this.propertyRegionHeight = new PropertyInt(
                "regionHeight",
                optionCategory.getRegionHeight(),
                1,
                optionCategory.getScreenHeight(),
                new WidgetInfoEditBox()
                        .pos((cursorBoundsX+15) + (cursorBoundsWidth - 30)/2 + 2, cursorBoundsY+45 + 4)
                        .size((cursorBoundsWidth - 30)/2 - 4, 15)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("visor.overlay.options.screen_region.height")))
        );

        widgetRegionX = propertyRegionX.createWidget();
        var responderX = widgetRegionX.responder;
        widgetRegionX.setResponder(
                (it)->{
                    responderX.accept(it);
                    optionCategory.setRegionX(propertyRegionX.getValue());
                }
        );

        widgetRegionY = propertyRegionY.createWidget();
        var responderY = widgetRegionY.responder;
        widgetRegionY.setResponder(
                (it)->{
                    responderY.accept(it);
                    optionCategory.setRegionY(propertyRegionY.getValue());
                }
        );

        widgetRegionWidth = propertyRegionWidth.createWidget();
        var responderWidth = widgetRegionWidth.responder;
        widgetRegionWidth.setResponder(
                (it)->{
                    responderWidth.accept(it);
                    optionCategory.setRegionWidth(propertyRegionWidth.getValue());
                }
        );

        widgetRegionHeight = propertyRegionHeight.createWidget();
        var responderHeight = widgetRegionHeight.responder;
        widgetRegionHeight.setResponder(
                (it)->{
                    responderHeight.accept(it);
                    optionCategory.setRegionHeight(propertyRegionHeight.getValue());
                }
        );

        this.addRenderableWidget(widgetRegionX);
        this.addRenderableWidget(widgetRegionY);
        this.addRenderableWidget(widgetRegionWidth);
        this.addRenderableWidget(widgetRegionHeight);
    }

    @Override
    public void tick() {
        widgetRegionX.tick();
        widgetRegionY.tick();
        widgetRegionWidth.tick();
        widgetRegionHeight.tick();
    }

    @Override
    protected void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        computePreviewArea();
        drawFramebufferPreview(guiGraphics);
        drawInteractiveRegionOverlay(guiGraphics);
    }

    private void computePreviewArea() {
        // Area inside background
        int left = (background != Background.EMPTY && background.getTexture() != null)
                ? cursorBoundsX + PREVIEW_MARGIN
                : PREVIEW_MARGIN;
        int right = (background != Background.EMPTY && background.getTexture() != null)
                ? cursorBoundsX + cursorBoundsWidth - PREVIEW_MARGIN
                : width - PREVIEW_MARGIN;

        // Make sure preview top is below the input rows
        int fieldsBottom = ROW2_Y + FIELD_HEIGHT;
        int bgTop = (background != Background.EMPTY && background.getTexture() != null)
                ? cursorBoundsY + PREVIEW_MARGIN
                : PREVIEW_MARGIN;
        int top = Math.max(bgTop, fieldsBottom + PREVIEW_MARGIN);

        int bottom = (background != Background.EMPTY && background.getTexture() != null)
                ? cursorBoundsY + cursorBoundsHeight - PREVIEW_MARGIN
                : height - PREVIEW_MARGIN;

        int availW = Math.max(1, right - left);
        int availH = Math.max(1, bottom - top);

        int fbW = Math.max(1, optionCategory.getScreenWidth());
        int fbH = Math.max(1, optionCategory.getScreenHeight());

        double scale = Math.min(availW / (double) fbW, availH / (double) fbH);

        int dw = Math.max(1, (int) Math.floor(fbW * scale));
        int dh = Math.max(1, (int) Math.floor(fbH * scale));
        int px = left + (availW - dw) / 2;
        int py = top + (availH - dh) / 2;

        this.previewX = px;
        this.previewY = py;
        this.previewW = dw;
        this.previewH = dh;
        this.previewScale = scale;
    }

    private void drawFramebufferPreview(GuiGraphics gui) {
        RenderTarget target = optionCategory.getTargetSupplier().get();
        if (target == null || target.getColorTextureId() <= 0) {
            gui.fill(previewX, previewY, previewX + previewW, previewY + previewH, 0xFF202020);
            gui.renderOutline(previewX, previewY, previewW, previewH, 0x55FFFFFF);
            return;
        }

        gui.flush();

        RenderSystem.setShaderTexture(0, target.getColorTextureId());

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        float uMax = (float) target.viewWidth / (float) target.width;
        float vMax = (float) target.viewHeight / (float) target.height;

        Matrix4f pose = gui.pose().last().pose();
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        // bottom-left
        buf.vertex(pose, previewX, previewY + previewH, 0).uv(0.0f, 0.0f).endVertex();
        // bottom-right
        buf.vertex(pose, previewX + previewW, previewY + previewH, 0).uv(uMax, 0.0f).endVertex();
        // top-right
        buf.vertex(pose, previewX + previewW, previewY, 0).uv(uMax, vMax).endVertex();
        // top-left
        buf.vertex(pose, previewX, previewY, 0).uv(0.0f, vMax).endVertex();
        BufferUploader.drawWithShader(buf.end());

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        gui.renderOutline(previewX, previewY, previewW, previewH, 0x80FFFFFF);
    }

    private void drawInteractiveRegionOverlay(GuiGraphics gui) {
        // Map region rect to preview coordinates
        int rx = previewX + (int) Math.round(optionCategory.getRegionX() * previewScale);
        int ry = previewY + (int) Math.round(optionCategory.getRegionY() * previewScale);
        int rw = Math.max(1, (int) Math.round(optionCategory.getRegionWidth() * previewScale));
        int rh = Math.max(1, (int) Math.round(optionCategory.getRegionHeight() * previewScale));

        // Clamp to preview bounds
        if (rx < previewX) rx = previewX;
        if (ry < previewY) ry = previewY;
        if (rx + rw > previewX + previewW) rw = previewX + previewW - rx;
        if (ry + rh > previewY + previewH) rh = previewY + previewH - ry;

        // Darken outside the selected region
        int dark = 0x80000000; // 50% black
        // Top
        gui.fill(previewX, previewY, previewX + previewW, ry, dark);
        // Bottom
        gui.fill(previewX, ry + rh, previewX + previewW, previewY + previewH, dark);
        // Left
        gui.fill(previewX, ry, rx, ry + rh, dark);
        // Right
        gui.fill(rx + rw, ry, previewX + previewW, ry + rh, dark);

        // Selection border
        int border = 0xFFFFFFFF;
        gui.fill(rx, ry, rx + rw, ry + 1, border);
        gui.fill(rx, ry + rh - 1, rx + rw, ry + rh, border);
        gui.fill(rx, ry, rx + 1, ry + rh, border);
        gui.fill(rx + rw - 1, ry, rx + rw, ry + rh, border);

        // 4 corner knobs
        drawKnob(gui, rx, ry);                 // top-left
        drawKnob(gui, rx + rw, ry);            // top-right
        drawKnob(gui, rx, ry + rh);            // bottom-left
        drawKnob(gui, rx + rw, ry + rh);       // bottom-right
    }

    private void drawKnob(GuiGraphics gui, int cx, int cy) {
        int x1 = cx - KNOB_HALF;
        int y1 = cy - KNOB_HALF;
        int x2 = x1 + KNOB_SIZE;
        int y2 = y1 + KNOB_SIZE;

        // Outer dark border
        gui.fill(x1 - 1, y1 - 1, x2 + 1, y1, 0xFF000000);
        gui.fill(x1 - 1, y2, x2 + 1, y2 + 1, 0xFF000000);
        gui.fill(x1 - 1, y1, x1, y2, 0xFF000000);
        gui.fill(x2, y1, x2 + 1, y2, 0xFF000000);

        // Inner light square
        gui.fill(x1, y1, x2, y2, 0xFFFFFFFF);
    }

    // Hit-test helpers
    private DragHandle handleAt(int mouseX, int mouseY) {
        // Region rect in preview coords
        int rx = previewX + (int) Math.round(optionCategory.getRegionX() * previewScale);
        int ry = previewY + (int) Math.round(optionCategory.getRegionY() * previewScale);
        int rw = Math.max(1, (int) Math.round(optionCategory.getRegionWidth() * previewScale));
        int rh = Math.max(1, (int) Math.round(optionCategory.getRegionHeight() * previewScale));

        int tlx = rx;
        int tly = ry;
        int trx = rx + rw;
        int try_ = ry;
        int blx = rx;
        int bly = ry + rh;
        int brx = rx + rw;
        int bry = ry + rh;

        if (inKnob(mouseX, mouseY, tlx, tly)) return DragHandle.TOP_LEFT;
        if (inKnob(mouseX, mouseY, trx, try_)) return DragHandle.TOP_RIGHT;
        if (inKnob(mouseX, mouseY, blx, bly)) return DragHandle.BOTTOM_LEFT;
        if (inKnob(mouseX, mouseY, brx, bry)) return DragHandle.BOTTOM_RIGHT;
        return DragHandle.NONE;
    }

    private boolean inKnob(int mx, int my, int cx, int cy) {
        int x1 = cx - KNOB_HALF;
        int y1 = cy - KNOB_HALF;
        int x2 = x1 + KNOB_SIZE;
        int y2 = y1 + KNOB_SIZE;
        return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
    }

    private boolean inPreview(int mx, int my) {
        return mx >= previewX && mx <= previewX + previewW
                && my >= previewY && my <= previewY + previewH;
    }

    // Region update helper: update PropertyInt values, then normal onRender flow syncs to optionCategory
    private void setRegionProperties(int x, int y, int w, int h) {
        int sw = optionCategory.getScreenWidth();
        int sh = optionCategory.getScreenHeight();

        int nx = Math.max(0, Math.min(sw, x));
        int ny = Math.max(0, Math.min(sh, y));
        int nw = Math.max(1, Math.min(sw - nx, w));
        int nh = Math.max(1, Math.min(sh - ny, h));

        // Assuming PropertyInt supports setValue(int)

        widgetRegionX.setValue(String.valueOf(nx));
        widgetRegionY.setValue(String.valueOf(ny));
        widgetRegionWidth.setValue(String.valueOf(nw));
        widgetRegionHeight.setValue(String.valueOf(nh));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean base = super.mouseClicked(mouseX, mouseY, button);
        if (button != 0) return base;

        if (!inPreview((int) mouseX, (int) mouseY)) {
            return base;
        }

        DragHandle handle = handleAt((int) mouseX, (int) mouseY);
        if (handle == DragHandle.NONE) {
            return base;
        }

        activeHandle = handle;
        dragStartMouseX = (int) mouseX;
        dragStartMouseY = (int) mouseY;
        startRegionX = optionCategory.getRegionX();
        startRegionY = optionCategory.getRegionY();
        startRegionW = optionCategory.getRegionWidth();
        startRegionH = optionCategory.getRegionHeight();
        return true; // captured
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragDX, double dragDY) {
        if (activeHandle == DragHandle.NONE) {
            return super.mouseDragged(mouseX, mouseY, button, dragDX, dragDY);
        }
        if (button != 0) {
            return super.mouseDragged(mouseX, mouseY, button, dragDX, dragDY);
        }

        int dxPx = (int) Math.round((mouseX - dragStartMouseX) / Math.max(0.00001, previewScale));
        int dyPx = (int) Math.round((mouseY - dragStartMouseY) / Math.max(0.00001, previewScale));

        int newX = startRegionX;
        int newY = startRegionY;
        int newW = startRegionW;
        int newH = startRegionH;

        switch (activeHandle) {
            case TOP_LEFT: {
                newX = startRegionX + dxPx;
                newY = startRegionY + dyPx;
                newW = startRegionW - (newX - startRegionX);
                newH = startRegionH - (newY - startRegionY);
                break;
            }
            case TOP_RIGHT: {
                newY = startRegionY + dyPx;
                newW = startRegionW + dxPx;
                newH = startRegionH - (newY - startRegionY);
                break;
            }
            case BOTTOM_LEFT: {
                newX = startRegionX + dxPx;
                newW = startRegionW - (newX - startRegionX);
                newH = startRegionH + dyPx;
                break;
            }
            case BOTTOM_RIGHT: {
                newW = startRegionW + dxPx;
                newH = startRegionH + dyPx;
                break;
            }
            case NONE:
                break;
        }

        setRegionProperties(newX, newY, newW, newH);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean base = super.mouseReleased(mouseX, mouseY, button);
        if (button == 0) {
            activeHandle = DragHandle.NONE;
        }
        return base;
    }
}