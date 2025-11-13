package me.phoenixra.visor.api.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoTextBoxEditable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class TextBoxEditable extends AbstractWidget {
    private static final int CURSOR_INSERT_COLOR = -3092272;
    private static final int SELECTION_COLOR = 0x803F6EFF;

    @Setter
    @Getter
    private boolean readOnly = false;
    @Setter
    @Nullable
    private Consumer<String> responder;

    private int frame = 0;

    protected final GuiTexture background;
    protected final Font font;
    protected final int textColor;
    protected final float textScale;
    protected final int paddingX = 4;
    protected final int paddingY = 4;

    private List<String> lines = new ArrayList<>(Collections.singletonList(""));

    private List<VisualLine> visualLines = new ArrayList<>();

    private int cursorLine = 0;
    private int cursorColumn = 0;
    private int anchorLine = 0;
    private int anchorColumn = 0;

    private int cachedVisualLineIndex = 0;

    private int scrollOffset = 0;

    private int scrollBarWidth = 6;
    private static final int SCROLLBAR_PADDING = 2;
    private static final int SCROLLBAR_COLOR = 0xFF8B8B8B;
    private static final int SCROLLBAR_HOVER_COLOR = 0xFFA0A0A0;
    private static final int SCROLLBAR_DRAG_COLOR = 0xFFB8B8B8;
    private static final int SCROLLBAR_BACKGROUND_COLOR = 0xFF3C3C3C;

    private boolean isDraggingScrollbar = false;
    private double dragStartY = 0;
    private int dragStartOffset = 0;
    private boolean scrollbarClicked = false;

    private static class VisualLine {
        final int logicalLineIndex;
        final int startColumn;
        final int endColumn;
        final String text;

        VisualLine(int logicalLineIndex, int startColumn, int endColumn, String text) {
            this.logicalLineIndex = logicalLineIndex;
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            this.text = text;
        }
    }

    public TextBoxEditable(@NotNull WidgetInfoTextBoxEditable widgetInfo) {
        super(
                widgetInfo.getX(),
                widgetInfo.getY(),
                widgetInfo.getWidth(),
                widgetInfo.getHeight(),
                Component.empty()
        );

        this.background = widgetInfo.getBackground();
        this.font = widgetInfo.getTextFont();
        this.textColor = widgetInfo.getTextColor().toInt();
        this.textScale = widgetInfo.getTextScale() <= 0f ? 1.0f : widgetInfo.getTextScale();
        this.scrollBarWidth = widgetInfo.getScrollBarWidth();

        if (widgetInfo.getText() != null) {
            String text = widgetInfo.getText().getString();
            this.lines = new ArrayList<>(Arrays.asList(text.split("\n")));
            if (this.lines.isEmpty()) {
                this.lines.add("");
            }
        }
        rebuildLines();
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (background != null) {
            background.blit(guiGraphics, getX(), getY(), width, height);
        }

        int textX = getX() + paddingX;
        int textY = getY() + paddingY;
        int textMaxX = getX() + width - paddingX;
        int textMaxY = getY() + height - paddingY;

        guiGraphics.enableScissor(textX, textY, textMaxX, textMaxY);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(textX, textY, 0);
        poseStack.scale(textScale, textScale, 1.0f);

        if (this.anchorLine != this.cursorLine || this.anchorColumn != this.cursorColumn) {
            renderSelection(guiGraphics);
        }

        int lineHeight = this.font.lineHeight + 2;

        int visibleHeight = (int)((textMaxY - textY) / textScale);
        int maxVisibleLines = visibleHeight / lineHeight;
        int startLine = Math.max(0, scrollOffset);
        int endLine = Math.min(visualLines.size(), scrollOffset + maxVisibleLines + 1);

        for (int i = startLine; i < endLine; i++) {
            VisualLine vLine = visualLines.get(i);
            int yOffset = (i - scrollOffset) * lineHeight;
            guiGraphics.drawString(this.font, vLine.text, 0, yOffset, textColor);
        }

        if (this.isFocused() && !this.readOnly && (this.frame / 10) % 2 == 0) {
            renderCursor(guiGraphics);
        }

        poseStack.popPose();
        guiGraphics.disableScissor();

        renderScrollbar(guiGraphics, mouseX, mouseY);
    }

    private void renderSelection(GuiGraphics guiGraphics) {
        int startLine = Math.min(anchorLine, cursorLine);
        int endLine = Math.max(anchorLine, cursorLine);
        int startColumn = (anchorLine < cursorLine || (anchorLine == cursorLine && anchorColumn < cursorColumn))
                ? anchorColumn : cursorColumn;
        int endColumn = (anchorLine < cursorLine || (anchorLine == cursorLine && anchorColumn < cursorColumn))
                ? cursorColumn : anchorColumn;

        int lineHeight = this.font.lineHeight + 2;

        for (int i = 0; i < visualLines.size(); i++) {
            VisualLine vLine = visualLines.get(i);

            if (vLine.logicalLineIndex < startLine || vLine.logicalLineIndex > endLine) {
                continue;
            }

            int y = (i - scrollOffset) * lineHeight;
            int selStartCol = 0;
            int selEndCol = vLine.text.length();

            if (vLine.logicalLineIndex == startLine) {
                if (startColumn >= vLine.startColumn && startColumn <= vLine.endColumn) {
                    selStartCol = startColumn - vLine.startColumn;
                } else if (startColumn > vLine.endColumn) {
                    continue;
                }
            }

            if (vLine.logicalLineIndex == endLine) {
                if (endColumn >= vLine.startColumn && endColumn <= vLine.endColumn) {
                    selEndCol = endColumn - vLine.startColumn;
                } else if (endColumn < vLine.startColumn) {
                    continue;
                }
            }

            int x1 = this.font.width(vLine.text.substring(0, selStartCol));
            int x2 = this.font.width(vLine.text.substring(0, selEndCol));
            guiGraphics.fill(x1, y, x2, y + this.font.lineHeight, SELECTION_COLOR);
        }
    }

    private void renderCursor(GuiGraphics guiGraphics) {
        int lineHeight = this.font.lineHeight + 2;

        int visualLineIndex = getVisualLineIndex(cursorLine, cursorColumn);
        if (visualLineIndex < 0) return;

        VisualLine vLine = visualLines.get(visualLineIndex);
        int columnInVisualLine = cursorColumn - vLine.startColumn;

        int cursorY = (visualLineIndex - scrollOffset) * lineHeight;
        int cursorX = this.font.width(vLine.text.substring(0, columnInVisualLine));

        guiGraphics.fill(cursorX, cursorY - 1, cursorX + 1, cursorY + this.font.lineHeight, CURSOR_INSERT_COLOR);
    }

    private void renderScrollbar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int textY = getY() + paddingY;
        int textMaxY = getY() + height - paddingY;
        int lineHeight = this.font.lineHeight + 2;
        int visibleHeight = (int)((textMaxY - textY) / textScale);
        int maxVisibleLines = visibleHeight / lineHeight;

        if (visualLines.size() <= maxVisibleLines) {
            return;
        }

        int scrollbarX = getX() + width - scrollBarWidth - SCROLLBAR_PADDING;
        int scrollbarY = getY() + SCROLLBAR_PADDING;
        int scrollbarHeight = height - SCROLLBAR_PADDING * 2;

        guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + scrollBarWidth, scrollbarY + scrollbarHeight, SCROLLBAR_BACKGROUND_COLOR);

        float contentRatio = (float) maxVisibleLines / visualLines.size();
        int minThumbHeight = 10;
        int thumbHeight = Math.max(minThumbHeight, (int)(scrollbarHeight * contentRatio));

        float scrollRatio = visualLines.size() > maxVisibleLines
                ? (float) scrollOffset / (visualLines.size() - maxVisibleLines)
                : 0;
        int thumbY = scrollbarY + (int)((scrollbarHeight - thumbHeight) * scrollRatio);

        int thumbColor = SCROLLBAR_COLOR;
        if (isDraggingScrollbar) {
            thumbColor = SCROLLBAR_DRAG_COLOR;
        } else if (isMouseOverScrollbarThumb(mouseX, mouseY, scrollbarX, thumbY, thumbHeight)) {
            thumbColor = SCROLLBAR_HOVER_COLOR;
        }

        guiGraphics.fill(scrollbarX, thumbY, scrollbarX + scrollBarWidth, thumbY + thumbHeight, thumbColor);
    }

    private boolean isMouseOverScrollbarThumb(int mouseX, int mouseY, int scrollbarX, int thumbY, int thumbHeight) {
        return mouseX >= scrollbarX && mouseX <= scrollbarX + scrollBarWidth &&
                mouseY >= thumbY && mouseY <= thumbY + thumbHeight;
    }

    private boolean isMouseOverScrollbar(int mouseX, int mouseY) {
        int scrollbarX = getX() + width - scrollBarWidth - SCROLLBAR_PADDING;
        int scrollbarY = getY() + SCROLLBAR_PADDING;
        int scrollbarHeight = height - SCROLLBAR_PADDING * 2;

        return mouseX >= scrollbarX && mouseX <= scrollbarX + scrollBarWidth &&
                mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight;
    }

    private int getVisualLineIndex(int logicalLine, int column) {
        if (visualLines.isEmpty()) return -1;

        if (cachedVisualLineIndex >= 0 && cachedVisualLineIndex < visualLines.size()) {
            VisualLine cachedLine = visualLines.get(cachedVisualLineIndex);
            if (cachedLine.logicalLineIndex == logicalLine &&
                    column >= cachedLine.startColumn && column <= cachedLine.endColumn) {
                return cachedVisualLineIndex;
            }

            if (cachedVisualLineIndex > 0) {
                VisualLine prevLine = visualLines.get(cachedVisualLineIndex - 1);
                if (prevLine.logicalLineIndex == logicalLine &&
                        column >= prevLine.startColumn && column <= prevLine.endColumn) {
                    cachedVisualLineIndex--;
                    return cachedVisualLineIndex;
                }
            }
            if (cachedVisualLineIndex < visualLines.size() - 1) {
                VisualLine nextLine = visualLines.get(cachedVisualLineIndex + 1);
                if (nextLine.logicalLineIndex == logicalLine &&
                        column >= nextLine.startColumn && column <= nextLine.endColumn) {
                    cachedVisualLineIndex++;
                    return cachedVisualLineIndex;
                }
            }
        }

        for (int i = 0; i < visualLines.size(); i++) {
            VisualLine vLine = visualLines.get(i);
            if (vLine.logicalLineIndex < logicalLine) continue;
            if (vLine.logicalLineIndex > logicalLine) break;

            if (column >= vLine.startColumn && column <= vLine.endColumn) {
                cachedVisualLineIndex = i;
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

    public void insertText(String text) {
        if (readOnly) return;

        deleteSelection();

        if (text.contains("\n")) {
            String[] textLines = text.split("\n", -1);

            String currentLine = lines.get(cursorLine);
            String beforeCursor = currentLine.substring(0, cursorColumn);
            String afterCursor = currentLine.substring(cursorColumn);

            lines.set(cursorLine, beforeCursor + textLines[0]);

            for (int i = 1; i < textLines.length - 1; i++) {
                lines.add(cursorLine + i, textLines[i]);
            }

            if (textLines.length > 1) {
                lines.add(cursorLine + textLines.length - 1, textLines[textLines.length - 1] + afterCursor);
                cursorLine += textLines.length - 1;
                cursorColumn = textLines[textLines.length - 1].length();
            } else {
                cursorColumn += textLines[0].length();
            }
        } else {
            String currentLine = lines.get(cursorLine);
            String newLine = currentLine.substring(0, cursorColumn) + text + currentLine.substring(cursorColumn);
            lines.set(cursorLine, newLine);
            cursorColumn += text.length();
            int maxWidth = (int)((width - paddingX * 2) / textScale);
            if (font.width(newLine) > maxWidth * textScale) {
                rebuildLines();
                anchorLine = cursorLine;
                anchorColumn = cursorColumn;
                scrollToCursor();
                onValueChange();
                return;
            }
        }

        rebuildLines();
        anchorLine = cursorLine;
        anchorColumn = cursorColumn;
        scrollToCursor();
        onValueChange();
    }

    private void deleteSelection() {
        if (anchorLine == cursorLine && anchorColumn == cursorColumn) return;

        int startLine = Math.min(anchorLine, cursorLine);
        int endLine = Math.max(anchorLine, cursorLine);

        int startColumn, endColumn;
        if (anchorLine < cursorLine || (anchorLine == cursorLine && anchorColumn < cursorColumn)) {
            startColumn = anchorColumn;
            endColumn = cursorColumn;
        } else {
            startColumn = cursorColumn;
            endColumn = anchorColumn;
        }

        if (startLine == endLine) {
            String line = lines.get(startLine);
            String newLine = line.substring(0, startColumn) + line.substring(endColumn);
            lines.set(startLine, newLine);
        } else {
            String firstPart = lines.get(startLine).substring(0, startColumn);
            String lastPart = lines.get(endLine).substring(endColumn);

            lines.set(startLine, firstPart + lastPart);

            for (int i = endLine; i > startLine; i--) {
                lines.remove(i);
            }
        }

        cursorLine = startLine;
        cursorColumn = startColumn;
        anchorLine = startLine;
        anchorColumn = startColumn;

        rebuildLines();
        onValueChange();
    }

    private void rebuildLines() {
        visualLines.clear();
        cachedVisualLineIndex = 0;

        int maxWidth = (int)((width - paddingX * 2) / textScale);

        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex);

            if (line.isEmpty()) {
                visualLines.add(new VisualLine(lineIndex, 0, 0, ""));
                continue;
            }

            int startCol = 0;
            while (startCol < line.length()) {
                String remaining = line.substring(startCol);
                int fitLength = remaining.length();

                if (font.width(remaining) > maxWidth) {
                    fitLength = 0;
                    int testLength = 1;

                    while (testLength <= remaining.length()) {
                        String testStr = remaining.substring(0, testLength);
                        if (font.width(testStr) > maxWidth) {
                            break;
                        }
                        fitLength = testLength;
                        testLength++;
                    }

                    if (fitLength == 0) {
                        fitLength = 1;
                    }
                }

                int endCol = startCol + fitLength;
                String visualText = line.substring(startCol, endCol);
                visualLines.add(new VisualLine(lineIndex, startCol, endCol, visualText));

                startCol = endCol;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!canConsumeInput() || readOnly) return false;

        if (Screen.isCopy(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(getHighlightedText());
            return true;
        }
        else if (Screen.isPaste(keyCode)) {
            insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            return true;
        }
        else if (Screen.isCut(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(getHighlightedText());
            insertText("");
            return true;
        }
        else if (Screen.isSelectAll(keyCode)) {
            anchorLine = 0;
            anchorColumn = 0;
            cursorLine = lines.size() - 1;
            cursorColumn = lines.get(cursorLine).length();
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!this.lines.isEmpty()) {
                int startLine = Math.min(this.cursorLine, this.anchorLine);
                int endLine = Math.max(this.cursorLine, this.anchorLine);
                int startColumn = Math.min(this.cursorColumn, this.anchorColumn);
                int endColumn = Math.max(this.cursorColumn, this.anchorColumn);

                if (startLine == endLine && startColumn == endColumn) {
                    if (this.cursorColumn > 0) {
                        startColumn--;
                    } else if (this.cursorLine > 0) {
                        startLine--;
                        startColumn = lines.get(startLine).length();
                    } else {
                        return true;
                    }
                }

                if (startLine == endLine) {
                    String line = lines.get(startLine);
                    String newLine = line.substring(0, startColumn) + line.substring(endColumn);
                    lines.set(startLine, newLine);
                } else {
                    String firstPart = lines.get(startLine).substring(0, startColumn);
                    String lastPart = lines.get(endLine).substring(endColumn);

                    lines.set(startLine, firstPart + lastPart);

                    for (int i = endLine; i > startLine; i--) {
                        lines.remove(i);
                    }
                }

                this.cursorLine = startLine;
                this.cursorColumn = startColumn;
                this.anchorLine = this.cursorLine;
                this.anchorColumn = this.cursorColumn;
                rebuildLines();
                scrollToCursor();
                onValueChange();
            }
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (!this.lines.isEmpty() && this.cursorLine < this.lines.size()) {
                int startLine = Math.min(this.cursorLine, this.anchorLine);
                int endLine = Math.max(this.cursorLine, this.anchorLine);
                int startColumn = Math.min(this.cursorColumn, this.anchorColumn);
                int endColumn = Math.max(this.cursorColumn, this.anchorColumn);

                if (startLine == endLine && startColumn == endColumn) {
                    if (this.cursorColumn < lines.get(this.cursorLine).length()) {
                        endColumn++;
                    } else if (this.cursorLine < lines.size() - 1) {
                        endLine++;
                        endColumn = 0;
                    }
                }

                if (startLine == endLine) {
                    String line = lines.get(startLine);
                    String newLine = line.substring(0, startColumn) + line.substring(endColumn);
                    lines.set(startLine, newLine);
                } else {
                    String firstPart = lines.get(startLine).substring(0, startColumn);
                    String lastPart = lines.get(endLine).substring(endColumn);

                    lines.set(startLine, firstPart + lastPart);

                    for (int i = endLine; i > startLine; i--) {
                        lines.remove(i);
                    }
                }

                this.cursorLine = startLine;
                this.cursorColumn = startColumn;
                this.anchorLine = this.cursorLine;
                this.anchorColumn = this.cursorColumn;
                rebuildLines();
                scrollToCursor();
                onValueChange();
            }
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (this.cursorLine > 0 || this.cursorColumn > 0) {
                if (this.cursorColumn > 0) {
                    this.cursorColumn--;
                    if (cachedVisualLineIndex >= 0 && cachedVisualLineIndex < visualLines.size()) {
                        VisualLine cached = visualLines.get(cachedVisualLineIndex);
                        if (cached.logicalLineIndex != cursorLine ||
                                cursorColumn < cached.startColumn) {
                            cachedVisualLineIndex = getVisualLineIndex(cursorLine, cursorColumn);
                        }
                    }
                } else {
                    this.cursorLine--;
                    this.cursorColumn = lines.get(this.cursorLine).length();
                    cachedVisualLineIndex = getVisualLineIndex(cursorLine, cursorColumn);
                }
                if (!Screen.hasShiftDown()) {
                    this.anchorLine = this.cursorLine;
                    this.anchorColumn = this.cursorColumn;
                }
                scrollToCursor();
            }
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (this.cursorLine < lines.size() - 1 || this.cursorColumn < lines.get(this.cursorLine).length()) {
                if (this.cursorColumn < lines.get(this.cursorLine).length()) {
                    this.cursorColumn++;
                    if (cachedVisualLineIndex >= 0 && cachedVisualLineIndex < visualLines.size()) {
                        VisualLine cached = visualLines.get(cachedVisualLineIndex);
                        if (cached.logicalLineIndex != cursorLine ||
                                cursorColumn > cached.endColumn) {
                            cachedVisualLineIndex = getVisualLineIndex(cursorLine, cursorColumn);
                        }
                    }
                } else {
                    this.cursorLine++;
                    this.cursorColumn = 0;
                    cachedVisualLineIndex = getVisualLineIndex(cursorLine, cursorColumn);
                }
                if (!Screen.hasShiftDown()) {
                    this.anchorLine = this.cursorLine;
                    this.anchorColumn = this.cursorColumn;
                }
                scrollToCursor();
            }
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_ENTER) {
            deleteSelection();

            String currentLine = lines.get(cursorLine);
            String beforeCursor = currentLine.substring(0, cursorColumn);
            String afterCursor = currentLine.substring(cursorColumn);

            lines.set(cursorLine, beforeCursor);
            lines.add(cursorLine + 1, afterCursor);

            cursorLine++;
            cursorColumn = 0;
            anchorLine = cursorLine;
            anchorColumn = cursorColumn;
            rebuildLines();
            scrollToCursor();
            onValueChange();
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_UP) {
            int currentVisualLine = cachedVisualLineIndex;
            VisualLine currentVLine = visualLines.get(currentVisualLine);

            if (currentVLine.logicalLineIndex != cursorLine ||
                    cursorColumn < currentVLine.startColumn ||
                    cursorColumn > currentVLine.endColumn) {
                currentVisualLine = getVisualLineIndex(cursorLine, cursorColumn);
            }

            if (currentVisualLine > 0) {
                VisualLine currentLine = visualLines.get(currentVisualLine);
                VisualLine prevVLine = visualLines.get(currentVisualLine - 1);

                int currentColumnInVisual = cursorColumn - currentLine.startColumn;
                int newColumnInVisual = Math.min(currentColumnInVisual, prevVLine.text.length());

                cursorLine = prevVLine.logicalLineIndex;
                cursorColumn = prevVLine.startColumn + newColumnInVisual;
                cachedVisualLineIndex = currentVisualLine - 1;
            }

            if (!Screen.hasShiftDown()) {
                this.anchorLine = this.cursorLine;
                this.anchorColumn = this.cursorColumn;
            }
            scrollToCursor();
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_DOWN) {
            int currentVisualLine = cachedVisualLineIndex;
            VisualLine currentVLine = visualLines.get(currentVisualLine);

            if (currentVLine.logicalLineIndex != cursorLine ||
                    cursorColumn < currentVLine.startColumn ||
                    cursorColumn > currentVLine.endColumn) {
                currentVisualLine = getVisualLineIndex(cursorLine, cursorColumn);
            }

            if (currentVisualLine >= 0 && currentVisualLine < visualLines.size() - 1) {
                VisualLine currentLine = visualLines.get(currentVisualLine);
                VisualLine nextVLine = visualLines.get(currentVisualLine + 1);

                int currentColumnInVisual = cursorColumn - currentLine.startColumn;
                int newColumnInVisual = Math.min(currentColumnInVisual, nextVLine.text.length());

                cursorLine = nextVLine.logicalLineIndex;
                cursorColumn = nextVLine.startColumn + newColumnInVisual;
                cachedVisualLineIndex = currentVisualLine + 1;
            }

            if (!Screen.hasShiftDown()) {
                this.anchorLine = this.cursorLine;
                this.anchorColumn = this.cursorColumn;
            }
            scrollToCursor();
            return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!canConsumeInput() || readOnly) return false;

        if (codePoint == '\u0000' || Character.isISOControl(codePoint)) return false;

        insertText(String.valueOf(codePoint));
        return true;
    }

    private boolean canConsumeInput() {
        return this.active && this.visible;
    }

    private String getHighlightedText() {
        if (anchorLine == cursorLine && anchorColumn == cursorColumn) {
            return "";
        }

        int startLine = Math.min(anchorLine, cursorLine);
        int endLine = Math.max(anchorLine, cursorLine);
        int startColumn = (anchorLine < cursorLine || (anchorLine == cursorLine && anchorColumn < cursorColumn))
                ? anchorColumn : cursorColumn;
        int endColumn = (anchorLine < cursorLine || (anchorLine == cursorLine && anchorColumn < cursorColumn))
                ? cursorColumn : anchorColumn;

        StringBuilder highlightedText = new StringBuilder();

        if (startLine == endLine) {
            String line = lines.get(startLine);
            if (startColumn < line.length() && endColumn <= line.length()) {
                highlightedText.append(line, startColumn, endColumn);
            }
        } else {
            String firstLine = lines.get(startLine);
            if (startColumn < firstLine.length()) {
                highlightedText.append(firstLine.substring(startColumn)).append('\n');
            }

            for (int i = startLine + 1; i < endLine; i++) {
                highlightedText.append(lines.get(i)).append('\n');
            }

            String lastLine = lines.get(endLine);
            if (endColumn <= lastLine.length()) {
                highlightedText.append(lastLine.substring(0, endColumn));
            }
        }

        return highlightedText.toString();
    }

    public void tick() {
        ++this.frame;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!canConsumeInput()) return false;

        boolean clicked = this.isMouseOver(mouseX, mouseY);
        if (clicked && button == 0) {
            if (isMouseOverScrollbar((int)mouseX, (int)mouseY)) {
                scrollbarClicked = true;
                isDraggingScrollbar = true;
                dragStartY = mouseY;
                dragStartOffset = scrollOffset;

                int textY = getY() + paddingY;
                int textMaxY = getY() + height - paddingY;
                int lineHeight = this.font.lineHeight + 2;
                int visibleHeight = (int)((textMaxY - textY) / textScale);
                int maxVisibleLines = visibleHeight / lineHeight;

                int scrollbarY = getY() + SCROLLBAR_PADDING;
                int scrollbarHeight = height - SCROLLBAR_PADDING * 2;
                float contentRatio = (float) maxVisibleLines / visualLines.size();
                int minThumbHeight = 15;
                int thumbHeight = Math.max(minThumbHeight, (int)(scrollbarHeight * contentRatio));
                float scrollRatio = visualLines.size() > maxVisibleLines
                        ? (float) scrollOffset / (visualLines.size() - maxVisibleLines)
                        : 0;
                int thumbY = scrollbarY + (int)((scrollbarHeight - thumbHeight) * scrollRatio);

                if (!isMouseOverScrollbarThumb((int)mouseX, (int)mouseY,
                        getX() + width - scrollBarWidth - SCROLLBAR_PADDING, thumbY, thumbHeight)) {

                    float clickPos = (float)(mouseY - scrollbarY - thumbHeight / 2.0);
                    float availableTrack = scrollbarHeight - thumbHeight;
                    float clickRatio = Math.max(0, Math.min(1, clickPos / availableTrack));
                    int maxScroll = Math.max(0, visualLines.size() - maxVisibleLines);
                    scrollOffset = (int)(clickRatio * maxScroll);
                    clampScrollOffset();
                }

                return true;
            }

            if (readOnly) return false;

            int textX = getX() + paddingX;
            int textY = getY() + paddingY;

            int relativeX = (int) ((mouseX - textX) / this.textScale);
            int relativeY = (int) ((mouseY - textY) / this.textScale);

            int lineHeight = this.font.lineHeight + 2;
            int visualLineIndex = relativeY / lineHeight + scrollOffset;
            if (visualLineIndex < 0) visualLineIndex = 0;
            if (visualLineIndex >= visualLines.size()) visualLineIndex = visualLines.size() - 1;

            VisualLine vLine = visualLines.get(visualLineIndex);

            int colInVisualLine = 0;
            for (int i = 0; i <= vLine.text.length(); i++) {
                String substr = vLine.text.substring(0, i);
                int charWidth = this.font.width(substr);

                if (i < vLine.text.length()) {
                    String nextSubstr = vLine.text.substring(0, i + 1);
                    int nextCharWidth = this.font.width(nextSubstr);
                    int midPoint = (charWidth + nextCharWidth) / 2;

                    if (relativeX < midPoint) {
                        colInVisualLine = i;
                        break;
                    }
                } else {
                    colInVisualLine = i;
                    break;
                }
            }

            if (colInVisualLine > vLine.text.length()) colInVisualLine = vLine.text.length();

            this.cursorLine = vLine.logicalLineIndex;
            this.cursorColumn = vLine.startColumn + colInVisualLine;
            this.anchorLine = this.cursorLine;
            this.anchorColumn = this.cursorColumn;
            this.cachedVisualLineIndex = visualLineIndex;
        }
        return clicked;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!canConsumeInput()) return false;

        if (button == 0) {
            if (isDraggingScrollbar) {
                int textY = getY() + paddingY;
                int textMaxY = getY() + height - paddingY;
                int lineHeight = this.font.lineHeight + 2;
                int visibleHeight = (int)((textMaxY - textY) / textScale);
                int maxVisibleLines = visibleHeight / lineHeight;

                int scrollbarHeight = height - SCROLLBAR_PADDING * 2;

                double deltaY = mouseY - dragStartY;
                int maxScroll = Math.max(0, visualLines.size() - maxVisibleLines);

                if (maxScroll > 0) {
                    float scrollbarRatio = (float)deltaY / scrollbarHeight;
                    int scrollDelta = (int)(scrollbarRatio * maxScroll);
                    scrollOffset = dragStartOffset + scrollDelta;
                    clampScrollOffset();
                }

                return true;
            }

            if (readOnly) return false;

            int textX = getX() + paddingX;
            int textY = getY() + paddingY;

            int relativeX = (int) ((mouseX - textX) / this.textScale);
            int relativeY = (int) ((mouseY - textY) / this.textScale);

            int lineHeight = this.font.lineHeight + 2;
            int visualLineIndex = relativeY / lineHeight + scrollOffset;
            if (visualLineIndex < 0) visualLineIndex = 0;
            if (visualLineIndex >= visualLines.size()) visualLineIndex = visualLines.size() - 1;

            VisualLine vLine = visualLines.get(visualLineIndex);

            int colInVisualLine = 0;
            for (int i = 0; i <= vLine.text.length(); i++) {
                String substr = vLine.text.substring(0, i);
                int charWidth = this.font.width(substr);

                if (i < vLine.text.length()) {
                    String nextSubstr = vLine.text.substring(0, i + 1);
                    int nextCharWidth = this.font.width(nextSubstr);
                    int midPoint = (charWidth + nextCharWidth) / 2;

                    if (relativeX < midPoint) {
                        colInVisualLine = i;
                        break;
                    }
                } else {
                    colInVisualLine = i;
                    break;
                }
            }

            if (colInVisualLine > vLine.text.length()) colInVisualLine = vLine.text.length();

            this.cursorLine = vLine.logicalLineIndex;
            this.cursorColumn = vLine.startColumn + colInVisualLine;
            this.cachedVisualLineIndex = visualLineIndex;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isDraggingScrollbar) {
            isDraggingScrollbar = false;
            scrollbarClicked = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        int scrollAmount = (int) -scrollDelta * 3;
        scrollOffset += scrollAmount;
        clampScrollOffset();
        return true;
    }

    private void clampScrollOffset() {
        int textMaxY = getY() + height - paddingY;
        int textY = getY() + paddingY;
        int lineHeight = this.font.lineHeight + 2;
        int visibleHeight = (int)((textMaxY - textY) / textScale);
        int maxVisibleLines = visibleHeight / lineHeight;

        int maxScroll = Math.max(0, visualLines.size() - maxVisibleLines);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    private void scrollToCursor() {
        int visualLineIndex = getVisualLineIndex(cursorLine, cursorColumn);
        if (visualLineIndex < 0) return;

        int textMaxY = getY() + height - paddingY;
        int textY = getY() + paddingY;
        int lineHeight = this.font.lineHeight + 2;
        int visibleHeight = (int)((textMaxY - textY) / textScale);
        int maxVisibleLines = visibleHeight / lineHeight;

        if (visualLineIndex >= scrollOffset + maxVisibleLines) {
            scrollOffset = visualLineIndex - maxVisibleLines + 1;
        }

        if (visualLineIndex < scrollOffset) {
            scrollOffset = visualLineIndex;
        }

        clampScrollOffset();
    }

    public void setValue (String value) {
        this.lines = new ArrayList<>(Arrays.asList(value.split("\n", -1)));
        this.cursorLine = lines.size() - 1;
        this.cursorColumn = lines.get(cursorLine).length();
        this.anchorLine = cursorLine;
        this.anchorColumn = cursorColumn;
        rebuildLines();
        scrollToCursor();
        onValueChange();
    }

    public String getValue() {
        return String.join("\n", lines);
    }

    private void onValueChange() {
        if (this.responder != null) {
            this.responder.accept(getValue());
        }
    }

    private void showKeyboard(){
        if (VisorAPI.clientState().stateMode().isActive()) {
            var keyboardAccessor = VisorAPI.client().getGuiManager()
                    .getOverlayManager()
                    .getKeyboardAccessor();
            var cursorHandler = VisorAPI.client().getGuiManager().getCursorHandler();
            if (cursorHandler.isCursorHandFocused()) {
                VROverlayScreen overlayBase = null;
                if (cursorHandler.getFocusedOverlay() instanceof VROverlayScreen overlayScreen) {
                    overlayBase = overlayScreen;
                }
                Screen screenFocused = overlayBase == null
                        ? Minecraft.getInstance().screen
                        : overlayBase;
                keyboardAccessor.showKeyboard(screenFocused);
            }
        }
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused && !this.readOnly && !scrollbarClicked) {
            showKeyboard();
        }
    }
}
