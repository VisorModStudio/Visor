package org.vmstudio.visor.core.client.gui.screens.overlayoptions;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsScreenRegion;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoValueDrag;
import org.vmstudio.visor.api.client.gui.widgets.sets.ValueEditorInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class OptionsScreenRegion extends OptionsScreen<OverlayOptionsScreenRegion> {
    private static final int FIELD_HEIGHT = 15;
    private static final int ROW2_Y = 60;

    // Layout gaps
    private static final int IN_GROUP_GAP = 10;
    private static final int BETWEEN_GROUPS_GAP = 20;

    private static final int PREVIEW_MARGIN = 8;

    private static final int KNOB_SIZE = 8;
    private static final int KNOB_HALF = KNOB_SIZE / 2;


    private static final int EDGE_GRAB_SLOP = 4;

    private static final int MIN_INNER_MOVE_PX = KNOB_SIZE;

    private enum DragHandle {
        NONE,
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
        TOP, BOTTOM, LEFT, RIGHT,
        MOVE_WHOLE
    }



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

    private int previewRegionStartY;

    private ValueEditorInt editorRegionX;
    private ValueEditorInt editorRegionY;
    private ValueEditorInt editorRegionWidth;
    private ValueEditorInt editorRegionHeight;


    private int lastX = 0;
    private int lastY = 0;
    private int lastWidth = 0;
    private int lastHeight = 0;

    public OptionsScreenRegion(@NotNull OverlayOptionsScreenRegion optionsGroup) {
        super(optionsGroup, Background.VERTICAL_WIDER);
    }

    @Override
    protected void onInit() {

        int fieldWidth = (cursorBoundsWidth - 30) / 2;
        int fieldX = cursorBoundsX + (cursorBoundsWidth - fieldWidth) / 2;

        int yStart = cursorBoundsY + 20;
        int yRegionY = yStart + FIELD_HEIGHT + IN_GROUP_GAP;
        int yRegionWidth = yRegionY + FIELD_HEIGHT + BETWEEN_GROUPS_GAP;
        int yRegionHeight = yRegionWidth + FIELD_HEIGHT + IN_GROUP_GAP;

        this.previewRegionStartY = yRegionHeight + FIELD_HEIGHT;

        this.editorRegionX = new ValueEditorInt.Builder(
                optionsGroup.getRegionX(),
                fieldX,
                yStart,
                fieldWidth,
                FIELD_HEIGHT
        ).range(
                        0, optionsGroup.getScreenWidth()
                ).editBox(
                        new WidgetInfoEditBox()
                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.screen_region.x")))
                ).leftArrow(
                        new WidgetInfoValueDrag()
                                .setTexture(OptionTextures.ARROW_GRAY_LEFT)
                                .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                ).rightArrow(
                        new WidgetInfoValueDrag()
                                .setTexture(OptionTextures.ARROW_GRAY_RIGHT)
                                .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                ).setResponder((newRegionX)->{
                    int maxWidth = optionsGroup.getScreenWidth();
                    int currentWidth = optionsGroup.getRegionWidth();
                    int clamped = Mth.clamp(
                            currentWidth,
                            0,
                            maxWidth - newRegionX
                    );

                    if (clamped != currentWidth) {
                        editorRegionWidth.setValue(clamped, true);
                        optionsGroup.setRegionWidth(clamped);
                    }
                    optionsGroup.setRegionX(newRegionX);
                })
                .build();

        this.editorRegionY = new ValueEditorInt.Builder(
                optionsGroup.getRegionY(),
                fieldX,
                yRegionY,
                fieldWidth,
                FIELD_HEIGHT
        ).range(
                        0, optionsGroup.getScreenHeight()
                ).editBox(
                        new WidgetInfoEditBox()
                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.screen_region.y")))
                ).leftArrow(
                        new WidgetInfoValueDrag()
                                .setTexture(OptionTextures.ARROW_GRAY_LEFT)
                                .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                ).rightArrow(
                        new WidgetInfoValueDrag()
                                .setTexture(OptionTextures.ARROW_GRAY_RIGHT)
                                .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                ).setResponder((newRegionY)->{
                    int maxHeight = optionsGroup.getScreenHeight();
                    int currentHeight = optionsGroup.getRegionHeight();
                    int clamped = Mth.clamp(
                            currentHeight,
                            0,
                            maxHeight - newRegionY
                    );

                    if (clamped != currentHeight) {
                        editorRegionHeight.setValue(clamped, true);
                        optionsGroup.setRegionHeight(clamped);
                    }
                    optionsGroup.setRegionY(newRegionY);
                })
                .build();

        this.editorRegionWidth = new ValueEditorInt.Builder(
                optionsGroup.getRegionWidth(),
                fieldX,
                yRegionWidth,
                fieldWidth,
                FIELD_HEIGHT
        ).range(
                        0, optionsGroup.getScreenWidth()
                ).editBox(
                        new WidgetInfoEditBox()
                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.screen_region.width")))
                ).leftArrow(
                        new WidgetInfoValueDrag()
                                .setTexture(OptionTextures.ARROW_GRAY_LEFT)
                                .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                ).rightArrow(
                        new WidgetInfoValueDrag()
                                .setTexture(OptionTextures.ARROW_GRAY_RIGHT)
                                .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                ).setResponder((newRegionWidth)->{
                    int maxWidth = optionsGroup.getScreenWidth();
                    int regionX = optionsGroup.getRegionX();
                    int clamped = Mth.clamp(
                            newRegionWidth,
                            1,
                            maxWidth - regionX
                    );

                    if (clamped != newRegionWidth) {
                        editorRegionWidth.setValue(clamped, true);
                    }
                    optionsGroup.setRegionWidth(clamped);
                })
                .build();

        this.editorRegionHeight = new ValueEditorInt.Builder(
                optionsGroup.getRegionHeight(),
                fieldX,
                yRegionHeight,
                fieldWidth,
                FIELD_HEIGHT
        ).range(
                0, optionsGroup.getScreenHeight()
                ).editBox(
                        new WidgetInfoEditBox()
                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.screen_region.height")))
                ).leftArrow(
                        new WidgetInfoValueDrag()
                                .setTexture(OptionTextures.ARROW_GRAY_LEFT)
                                .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                ).rightArrow(
                        new WidgetInfoValueDrag()
                                .setTexture(OptionTextures.ARROW_GRAY_RIGHT)
                                .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                ).setResponder((newRegionHeight)->{
                    int maxHeight = optionsGroup.getScreenHeight();
                    int regionY = optionsGroup.getRegionY();
                    int clamped = Mth.clamp(
                            newRegionHeight,
                            1,
                            maxHeight - regionY
                    );

                    if (clamped != newRegionHeight) {
                        editorRegionHeight.setValue(clamped, true);
                    }
                    optionsGroup.setRegionHeight(clamped);
                })
                .build();

        lastX = optionsGroup.getRegionX();
        lastY = optionsGroup.getRegionY();
        lastWidth = optionsGroup.getRegionWidth();
        lastHeight = optionsGroup.getRegionHeight();
        editorRegionX.initWidgets().forEach(this::addRenderableWidget);
        editorRegionY.initWidgets().forEach(this::addRenderableWidget);
        editorRegionWidth.initWidgets().forEach(this::addRenderableWidget);
        editorRegionHeight.initWidgets().forEach(this::addRenderableWidget);
    }

    @Override
    public void tick() {
        int regionX = editorRegionX.getValue();
        int regionY = editorRegionY.getValue();
        int regionWidth = editorRegionWidth.getValue();
        int regionHeight = editorRegionHeight.getValue();
        if(lastX != regionX
                || lastY != regionY
                || lastWidth != regionWidth
                || lastHeight != regionHeight){
            var pose = optionsGroup.getOwner().getOption(
                    OverlayOptionsPose.ID, OverlayOptionsPose.class
            );
            if(pose != null){
                float factor = (float) Math.sqrt(
                        (regionWidth/(double)lastWidth)
                                * (regionHeight/(double)lastHeight)
                );
                pose.setScale(pose.getScale() * factor);
            }
            lastX = optionsGroup.getRegionX();
            lastY = optionsGroup.getRegionY();
            lastWidth = optionsGroup.getRegionWidth();
            lastHeight = optionsGroup.getRegionHeight();

        }
        editorRegionX.onTick();
        editorRegionY.onTick();
        editorRegionWidth.onTick();
        editorRegionHeight.onTick();
    }

    @Override
    protected void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int editBoxWidth = (cursorBoundsWidth - 30) / 2;
        int startPosX = cursorBoundsX + (cursorBoundsWidth - editBoxWidth) / 2;

        int textHeight = 8;
        int fieldWidth = (cursorBoundsWidth - 30) / 2;

        int xFieldPosY = cursorBoundsY + 12;
        int yFieldPosY = xFieldPosY + FIELD_HEIGHT + IN_GROUP_GAP;

        int widthFieldPosY = yFieldPosY + FIELD_HEIGHT + BETWEEN_GROUPS_GAP;
        int heightFieldPosY = widthFieldPosY + FIELD_HEIGHT + IN_GROUP_GAP;

        GuiHelper.renderScalableText(
                guiGraphics,
                Minecraft.getInstance().font,
                "x",
                AtumColor.LIGHT_GRAY.asInt(),
                startPosX, xFieldPosY,
                fieldWidth, textHeight,
                true
        );
        GuiHelper.renderScalableText(
                guiGraphics,
                Minecraft.getInstance().font,
                "y",
                AtumColor.LIGHT_GRAY.asInt(),
                startPosX, yFieldPosY,
                fieldWidth, textHeight,
                true
        );

        GuiHelper.renderScalableText(
                guiGraphics,
                Minecraft.getInstance().font,
                "w",
                AtumColor.LIGHT_GRAY.asInt(),
                startPosX, widthFieldPosY,
                fieldWidth, textHeight,
                true
        );
        GuiHelper.renderScalableText(
                guiGraphics,
                Minecraft.getInstance().font,
                "h",
                AtumColor.LIGHT_GRAY.asInt(),
                startPosX, heightFieldPosY,
                fieldWidth, textHeight,
                true
        );
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

        int bgTop = (background != Background.EMPTY && background.getTexture() != null)
                ? cursorBoundsY + PREVIEW_MARGIN
                : PREVIEW_MARGIN;
        int top = Math.max(bgTop, previewRegionStartY + PREVIEW_MARGIN);

        int bottom = (background != Background.EMPTY && background.getTexture() != null)
                ? cursorBoundsY + cursorBoundsHeight - PREVIEW_MARGIN
                : height - PREVIEW_MARGIN;

        int availW = Math.max(1, right - left);
        int availH = Math.max(1, bottom - top);

        int fbW = Math.max(1, optionsGroup.getScreenWidth());
        int fbH = Math.max(1, optionsGroup.getScreenHeight());

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
        RenderTarget target = optionsGroup.getTargetSupplier().get();
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
        BufferBuilder buf;
        buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        // bottom-left
        buf.addVertex(pose, previewX, previewY + previewH, 0).setUv(0.0f, 0.0f);
        // bottom-right
        buf.addVertex(pose, previewX + previewW, previewY + previewH, 0).setUv(uMax, 0.0f);
        // top-right
        buf.addVertex(pose, previewX + previewW, previewY, 0).setUv(uMax, vMax);
        // top-left
        buf.addVertex(pose, previewX, previewY, 0).setUv(0.0f, vMax);
        BufferUploader.drawWithShader(buf.buildOrThrow());

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        gui.renderOutline(previewX, previewY, previewW, previewH, 0x80FFFFFF);
    }

    private void drawInteractiveRegionOverlay(GuiGraphics gui) {
        // Map region rect to preview coordinates
        int rx = previewX + (int) Math.round(optionsGroup.getRegionX() * previewScale);
        int ry = previewY + (int) Math.round(optionsGroup.getRegionY() * previewScale);
        int rw = Math.max(1, (int) Math.round(optionsGroup.getRegionWidth() * previewScale));
        int rh = Math.max(1, (int) Math.round(optionsGroup.getRegionHeight() * previewScale));

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
        int rx = previewX + (int) Math.round(optionsGroup.getRegionX() * previewScale);
        int ry = previewY + (int) Math.round(optionsGroup.getRegionY() * previewScale);
        int rw = Math.max(1, (int) Math.round(optionsGroup.getRegionWidth() * previewScale));
        int rh = Math.max(1, (int) Math.round(optionsGroup.getRegionHeight() * previewScale));

        int tlx = rx;
        int tly = ry;
        int trx = rx + rw;
        int try_ = ry;
        int blx = rx;
        int bly = ry + rh;
        int brx = rx + rw;
        int bry = ry + rh;

        // Corner knobs first
        if (inKnob(mouseX, mouseY, tlx, tly)) return DragHandle.TOP_LEFT;
        if (inKnob(mouseX, mouseY, trx, try_)) return DragHandle.TOP_RIGHT;
        if (inKnob(mouseX, mouseY, blx, bly)) return DragHandle.BOTTOM_LEFT;
        if (inKnob(mouseX, mouseY, brx, bry)) return DragHandle.BOTTOM_RIGHT;

        // Then edges (lines between knobs)
        if (inHorizontalEdge(mouseX, mouseY, rx, rx + rw, ry)) return DragHandle.TOP;
        if (inHorizontalEdge(mouseX, mouseY, rx, rx + rw, ry + rh)) return DragHandle.BOTTOM;
        if (inVerticalEdge(mouseX, mouseY, ry, ry + rh, rx)) return DragHandle.LEFT;
        if (inVerticalEdge(mouseX, mouseY, ry, ry + rh, rx + rw)) return DragHandle.RIGHT;

        // Finally, inner area -> move whole region (only if it's not too small and has space inside)
        if (canMoveRegion(rw, rh) && inInnerArea(mouseX, mouseY, rx, ry, rw, rh)) {
            return DragHandle.MOVE_WHOLE;
        }

        return DragHandle.NONE;
    }

    private boolean inKnob(int mx, int my, int cx, int cy) {
        int x1 = cx - KNOB_HALF;
        int y1 = cy - KNOB_HALF;
        int x2 = x1 + KNOB_SIZE;
        int y2 = y1 + KNOB_SIZE;
        return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
    }

    private boolean inHorizontalEdge(int mx, int my, int x1, int x2, int y) {
        // Within horizontal span and close enough vertically
        return mx >= (x1 - EDGE_GRAB_SLOP) && mx <= (x2 + EDGE_GRAB_SLOP)
                && Math.abs(my - y) <= EDGE_GRAB_SLOP;
    }

    private boolean inVerticalEdge(int mx, int my, int y1, int y2, int x) {
        // Within vertical span and close enough horizontally
        return my >= (y1 - EDGE_GRAB_SLOP) && my <= (y2 + EDGE_GRAB_SLOP)
                && Math.abs(mx - x) <= EDGE_GRAB_SLOP;
    }

    private boolean inInnerArea(int mx, int my, int rx, int ry, int rw, int rh) {
        // Click inside the rectangle but away from the edges by EDGE_GRAB_SLOP
        return mx > rx + EDGE_GRAB_SLOP && mx < rx + rw - EDGE_GRAB_SLOP
                && my > ry + EDGE_GRAB_SLOP && my < ry + rh - EDGE_GRAB_SLOP;
    }

    private boolean canMoveRegion(int rw, int rh) {
        // Require some inner space so move doesn't collide with edges
        int innerW = rw - 2 * EDGE_GRAB_SLOP;
        int innerH = rh - 2 * EDGE_GRAB_SLOP;
        return innerW >= MIN_INNER_MOVE_PX && innerH >= MIN_INNER_MOVE_PX;
    }

    private boolean inPreview(int mx, int my) {
        return mx >= previewX && mx <= previewX + previewW
                && my >= previewY && my <= previewY + previewH;
    }

    private void setRegionProperties(int x, int y, int w, int h) {
        int screenWidth = optionsGroup.getScreenWidth();
        int screenHeight = optionsGroup.getScreenHeight();

        int newX = Math.max(0, Math.min(screenWidth, x));
        int newY = Math.max(0, Math.min(screenHeight, y));
        int newWidth = Math.max(1, Math.min(screenWidth - newX, w));
        int newHeight = Math.max(1, Math.min(screenHeight - newY, h));

        editorRegionX.setValue(newX, true);
        editorRegionY.setValue(newY, true);
        editorRegionWidth.setValue(newWidth, true);
        editorRegionHeight.setValue(newHeight, true);
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
        startRegionX = optionsGroup.getRegionX();
        startRegionY = optionsGroup.getRegionY();
        startRegionW = optionsGroup.getRegionWidth();
        startRegionH = optionsGroup.getRegionHeight();
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
            case TOP: {
                newY = startRegionY + dyPx;
                newH = startRegionH - (newY - startRegionY);
                break;
            }
            case BOTTOM: {
                newH = startRegionH + dyPx;
                break;
            }
            case LEFT: {
                newX = startRegionX + dxPx;
                newW = startRegionW - (newX - startRegionX);
                break;
            }
            case RIGHT: {
                newW = startRegionW + dxPx;
                break;
            }
            case MOVE_WHOLE: {
                // Move the entire region while keeping its size; clamp so size doesn't shrink at edges
                int sw = optionsGroup.getScreenWidth();
                int sh = optionsGroup.getScreenHeight();
                int targetX = startRegionX + dxPx;
                int targetY = startRegionY + dyPx;
                // clamp to keep full region inside bounds
                targetX = Math.max(0, Math.min(sw - startRegionW, targetX));
                targetY = Math.max(0, Math.min(sh - startRegionH, targetY));
                newX = targetX;
                newY = targetY;
                newW = startRegionW;
                newH = startRegionH;
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