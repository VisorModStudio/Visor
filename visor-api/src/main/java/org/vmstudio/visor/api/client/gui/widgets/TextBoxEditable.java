package org.vmstudio.visor.api.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoTextBoxEditable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextBoxEditable extends AbstractWidget {
    private static final int CURSOR_INSERT_COLOR = -3092272;
    private static final int LINE_PADDING = 2;

    private static final Pattern TOKEN_SPLIT = Pattern.compile("\\s+|\\S+");

    protected final GuiTexture background;
    protected final GuiTexture textureScrollBar;
    protected final GuiTexture textureScrollBarActive;

    protected final Font font;
    protected final int textColor;
    protected final int textHintColor;
    protected final int highlightColor;
    protected final float textScale;
    protected final int paddingX;
    protected final int paddingY;
    protected final int scrollBarWidth;

    @Getter
    private String value = "";
    private int maxLength;

    @Setter
    @Nullable
    private Component hint;
    @Setter
    @Nullable
    private Consumer<String> responder;
    @Setter
    private Predicate<String> filter;

    @Getter
    private boolean readOnly = false;

    private final List<String> textLines = new ArrayList<>();
    private final List<Integer> lineStartIndices = new ArrayList<>();
    private int cursorLine = 0;
    private int cursorColumn = 0;
    private int cursorPos;
    private int selectionAnchor;
    private boolean updateCursorCoordinates = true;
    protected boolean recalculateLines = true;

    @Getter
    protected int scrollOffset = 0; // Unscaled units (line height space)
    protected int maxScrollOffset = 0; // Unscaled units
    protected long lastScrollingCall = -1;
    protected boolean scrolling = false;

    // New: when dragging the thumb, remember where inside the thumb we grabbed
    private int thumbGrabOffset = -1;

    private boolean shiftPressed;
    private boolean followCaret = true; // auto-scroll to caret when caret moves

    private int frame;

    public TextBoxEditable(@NotNull WidgetInfoTextBoxEditable widgetInfo) {
        super(widgetInfo.getX(),
                widgetInfo.getY(),
                widgetInfo.getWidth(),
                widgetInfo.getHeight(),
                Component.empty()
        );

        this.background = widgetInfo.getBackground();
        this.textureScrollBar = widgetInfo.getTextureScrollBar();
        this.textureScrollBarActive = widgetInfo.getTextureScrollBarActive();

        this.font = widgetInfo.getTextFont();
        this.textColor = widgetInfo.getTextColor().asInt();
        this.textHintColor = widgetInfo.getTextHintColor().asInt();
        this.highlightColor = widgetInfo.getHighlightColor().asInt();
        this.textScale = widgetInfo.getTextScale() <= 0f ? 1.0f : widgetInfo.getTextScale();
        this.paddingX = 4;
        this.paddingY = 4;
        this.scrollBarWidth = widgetInfo.getScrollBarWidth();

        this.maxLength = widgetInfo.getMaxLength() > 0 ? widgetInfo.getMaxLength() : 32;
        this.value = widgetInfo.getText() != null ? widgetInfo.getText().getString() : "";
        this.hint = widgetInfo.getHint();
        this.filter = Objects::nonNull;

        this.cursorPos = 0;
        this.selectionAnchor = this.cursorPos;
    }

    public void tick() {
        ++this.frame;
    }

    private boolean caretVisible() {
        return this.isFocused() && !this.readOnly;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        calculateLines();
        if (scrolling && lastScrollingCall + 200 < System.currentTimeMillis()) {
            scrolling = false;
            lastScrollingCall = -1;
            thumbGrabOffset = -1;
        }

        if (background != null) {
            background.blit(guiGraphics, getX(), getY(), width, height);
        }

        int textX = getX() + paddingX;
        int textY = getY() + paddingY;
        int textMaxX = getX() + width - paddingX - scrollBarWidth;
        int textMaxY = getY() + height - paddingY;

        guiGraphics.enableScissor(textX, textY, textMaxX, textMaxY);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(textX, textY, 0);
        poseStack.scale(textScale, textScale, 1.0f);

        int lineHeight = getLineHeight();
        int lineY = -scrollOffset;

        if (this.value.isEmpty()) {
            if (this.hint != null && !this.isFocused()) {
                guiGraphics.drawString(this.font, this.hint, 0, lineY, textHintColor);
            } else if (caretVisible() && (this.frame / 6) % 2 == 0) {
                guiGraphics.drawString(this.font, "_", 0, lineY, this.textColor);
            }
        } else {
            if (updateCursorCoordinates) {
                updateCursorCoordinates();
            }

            int visibleHeightUnscaled = (int) ((textMaxY - textY) / textScale);

            for (int i = 0; i < textLines.size(); i++) {
                if (lineY + lineHeight >= 0 && lineY <= visibleHeightUnscaled) {
                    String lineText = textLines.get(i);
                    FormattedCharSequence line = FormattedCharSequence.forward(lineText, Style.EMPTY);

                    guiGraphics.drawString(this.font, line, 0, lineY, this.textColor);

                    if (!readOnly && isLineSelected(i)) {
                        renderSelectionHighlight(guiGraphics, i, lineY, lineHeight);
                    }

                    if (caretVisible() && (this.frame / 6) % 2 == 0 && i == cursorLine) {
                        int cursorX = getCursorPosX();
                        int lineVisualEnd = getLineVisualEndIndex(i);

                        boolean isCursorAtLineEnd = cursorPos == lineVisualEnd;

                        if (isCursorAtLineEnd) {
                            guiGraphics.drawString(this.font, "_", cursorX, lineY, this.textColor);
                        } else {
                            guiGraphics.fill(
                                    RenderType.guiOverlay(),
                                    cursorX,
                                    lineY + LINE_PADDING,
                                    cursorX + 1,
                                    lineY + font.lineHeight + LINE_PADDING,
                                    CURSOR_INSERT_COLOR
                            );
                        }
                    }
                }

                lineY += lineHeight;
            }
        }

        poseStack.popPose();
        guiGraphics.disableScissor();

        renderScrollBar(guiGraphics);
    }

    protected void renderScrollBar(@NotNull GuiGraphics guiGraphics) {
        if (maxScrollOffset <= 0) return;

        int trackX = getScrollbarX();
        int trackY = this.getY() + paddingY;
        int trackHeight = this.height - paddingY * 2;

        int contentHeightUnscaled = this.textLines.size() * getLineHeight();
        int visibleHeightUnscaled = (int) ((this.height - (paddingY * 2)) / textScale);

        if (contentHeightUnscaled <= 0 || trackHeight <= 0) return;

        int thumbHeight = Math.max(
                16,
                Math.min(
                        trackHeight,
                        (int) Math.round((double) visibleHeightUnscaled / (double) contentHeightUnscaled * trackHeight)
                )
        );

        int thumbY = trackY;
        if (this.maxScrollOffset > 0) {
            double ratio = (double) this.scrollOffset / (double) this.maxScrollOffset;
            thumbY = trackY + (int) Math.round((trackHeight - thumbHeight) * ratio);
        }

        GuiTexture scrollBarTex = scrolling ? textureScrollBarActive : textureScrollBar;

        if (scrollBarTex != null) {
            scrollBarTex.blit(guiGraphics, trackX, thumbY, scrollBarWidth, thumbHeight);
        } else {
            guiGraphics.fill(RenderType.guiOverlay(), trackX, thumbY, trackX + scrollBarWidth, thumbY + thumbHeight, 0x80000000);
        }
    }

    private void renderSelectionHighlight(GuiGraphics guiGraphics, int lineIndex, int lineY, int lineHeight) {
        int minCursor = Math.min(cursorPos, selectionAnchor);
        int maxCursor = Math.max(cursorPos, selectionAnchor);

        int lineStart = getLineStartIndex(lineIndex);
        int lineEnd = getLineEndIndex(lineIndex);

        if (maxCursor <= lineStart || minCursor >= lineEnd) return;

        int selStart = Math.max(minCursor, lineStart) - lineStart;
        int selEnd = Math.min(maxCursor, lineEnd) - lineStart;

        String lineText = textLines.get(lineIndex);
        int textLen = lineText.length();

        selStart = Mth.clamp(selStart, 0, textLen);
        selEnd = Mth.clamp(selEnd, 0, textLen);

        int startX = textLen == 0 ? 0 : this.font.width(lineText.substring(0, selStart));
        int endX = textLen == 0 ? this.font.width(" ") : this.font.width(lineText.substring(0, selEnd));

        var halfPadding = LINE_PADDING / 2;
        guiGraphics.fill(
                RenderType.guiTextHighlight(),
                startX,
                lineY - halfPadding,
                endX,
                lineY + halfPadding + font.lineHeight,
                highlightColor
        );
    }

    protected void calculateLines() {
        if (!recalculateLines) return;

        textLines.clear();
        lineStartIndices.clear();

        int textWidth = (int) ((this.width - (paddingX * 2) - scrollBarWidth) / textScale);
        if (textWidth <= 0) {
            recalculateLines = false;
            return;
        }

        if (value.isEmpty()) {
            lineStartIndices.add(0);
            textLines.add("");
            recalculateLines = false;
            updateCursorCoordinates = true;
            this.maxScrollOffset = 0;
            this.scrollOffset = 0;
            return;
        }

        String[] explicitLines = value.split("\n", -1);
        int currentPos = 0;

        for (int i = 0; i < explicitLines.length; i++) {
            String explicitLine = explicitLines[i];

            if (i > 0) {
                currentPos++; // account for newline char
            }
            if (explicitLine.isEmpty()) {
                textLines.add("");
                lineStartIndices.add(currentPos);
                continue;
            }

            List<String> tokens = new ArrayList<>();
            Matcher m = TOKEN_SPLIT.matcher(explicitLine);
            while (m.find()) tokens.add(m.group());

            int tokenIdx = 0;
            while (tokenIdx < tokens.size()) {
                lineStartIndices.add(currentPos);
                StringBuilder sb = new StringBuilder();
                int lineW = 0;

                while (tokenIdx < tokens.size()) {
                    String tok = tokens.get(tokenIdx);
                    int w = font.width(tok);

                    if (lineW + w <= textWidth) {
                        sb.append(tok);
                        lineW += w;
                        currentPos += tok.length();
                        tokenIdx++;
                    } else {
                        if (lineW == 0) {
                            String fit = font.plainSubstrByWidth(tok, textWidth);
                            if (!fit.isEmpty()) {
                                sb.append(fit);
                                currentPos += fit.length();
                                tokens.set(tokenIdx, tok.substring(fit.length()));
                            } else {
                                // Safety
                                tokens.set(tokenIdx, tok.substring(1));
                                currentPos += 1;
                                sb.append(tok.charAt(0));
                            }
                        }
                        break;
                    }
                }

                textLines.add(sb.toString());
            }
        }

        int scaledVisibleHeight = (int) ((this.height - (paddingY * 2)) / textScale);
        int totalTextHeightUnscaled = this.textLines.size() * getLineHeight();
        this.maxScrollOffset = Math.max(0, totalTextHeightUnscaled - scaledVisibleHeight);
        this.scrollOffset = Mth.clamp(this.scrollOffset, 0, this.maxScrollOffset);

        recalculateLines = false;
        updateCursorCoordinates = true;
    }

    private void updateCursorCoordinates() {
        if (textLines.isEmpty()) {
            cursorLine = 0;
            cursorColumn = 0;
            return;
        }

        cursorLine = -1;
        for (int i = 0; i < lineStartIndices.size(); i++) {
            int start = lineStartIndices.get(i);
            int end = (i < lineStartIndices.size() - 1)
                    ? lineStartIndices.get(i + 1)
                    : value.length();

            if (cursorPos >= start && cursorPos < end) {
                cursorLine = i;
                cursorColumn = cursorPos - start;
                break;
            }
        }

        if (cursorLine == -1) {
            cursorLine = lineStartIndices.size() - 1;
            cursorColumn = cursorPos - lineStartIndices.get(cursorLine);
        }

        if (!readOnly && followCaret) {
            ensureCursorVisible();
        }
        updateCursorCoordinates = false;
    }

    private void ensureCursorVisible() {
        int lineHeight = getLineHeight();
        int cursorY = cursorLine * lineHeight;

        int visibleTop = scrollOffset;
        int visibleBottom = scrollOffset + (int) ((height - (paddingY * 2)) / textScale);

        if (cursorY < visibleTop) {
            scrollOffset = cursorY;
        } else if (cursorY + lineHeight > visibleBottom) {
            scrollOffset = cursorY - (int) ((height - (paddingY * 2)) / textScale) + lineHeight;
        }

        scrollOffset = Mth.clamp(scrollOffset, 0, maxScrollOffset);
    }

    public void setValue(String text) {
        if (this.filter.test(text)) {
            if (text.length() > this.maxLength) {
                this.value = text.substring(0, this.maxLength);
            } else {
                this.value = text;
            }

            this.moveCursorToEnd();
            this.setSelectionAnchor(this.cursorPos);
            this.onValueChange(this.value);
        }
    }

    public void insertText(String textToWrite) {
        if (readOnly) return;

        int i = Math.min(this.cursorPos, this.selectionAnchor);
        int j = Math.max(this.cursorPos, this.selectionAnchor);
        int k = this.maxLength - this.value.length() - (i - j);
        if (k <= 0) return;

        String string = McVersionUtils.filterText(textToWrite, true);
        if (string.length() > k) string = string.substring(0, k);

        String string2 = new StringBuilder(this.value)
                .replace(i, j, string)
                .toString();
        if (this.filter.test(string2)) {
            this.value = string2;
            this.setCursorPosition(i + string.length());
            this.setSelectionAnchor(this.cursorPos);
            this.onValueChange(this.value);
        }
    }

    private void deleteText(int count) {
        if (readOnly) return;

        if (Screen.hasControlDown()) {
            this.deleteWords(count);
        } else {
            this.deleteChars(count);
        }
    }

    public void deleteWords(int num) {
        if (readOnly) return;

        if (!this.value.isEmpty()) {
            if (this.selectionAnchor != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(num) - this.cursorPos);
            }
        }
    }

    public void deleteChars(int num) {
        if (readOnly) return;

        if (!this.value.isEmpty()) {
            if (this.selectionAnchor != this.cursorPos) {
                this.insertText("");
            } else {
                int i = this.getCursorPos(num);
                int j = Math.min(i, this.cursorPos);
                int k = Math.max(i, this.cursorPos);
                if (j != k) {
                    String string = (new StringBuilder(this.value)).delete(j, k).toString();
                    if (this.filter.test(string)) {
                        this.value = string;
                        this.moveCursorTo(j);
                        this.onValueChange(this.value);
                    }
                }
            }
        }
    }

    private void onValueChange(String newText) {
        recalculateLines = true;
        if (this.responder != null) {
            this.responder.accept(newText);
        }
    }

    public void setCursorPosition(int pos) {
        this.cursorPos = Mth.clamp(pos, 0, this.value.length());
        followCaret = !readOnly; // only auto-follow when not read-only
        updateCursorCoordinates = true;
    }

    public void moveCursorTo(int pos) {
        this.setCursorPosition(pos);
        if (!this.shiftPressed) {
            this.setSelectionAnchor(this.cursorPos);
        }
    }

    public void moveCursor(int delta) {
        this.moveCursorTo(this.getCursorPos(delta));
    }

    private void moveCursorVertical(int lines) {
        if (textLines.isEmpty()) return;

        updateCursorCoordinates();

        int targetLine = Mth.clamp(cursorLine + lines, 0, textLines.size() - 1);

        if (targetLine != cursorLine) {
            String targetLineText = textLines.get(targetLine);
            String currentLineText = textLines.get(cursorLine);
            int cursorX = font.width(currentLineText.substring(0, Math.min(cursorColumn, currentLineText.length())));
            int targetColumn = font.plainSubstrByWidth(targetLineText, cursorX).length();
            int newPos = lineStartIndices.get(targetLine) + targetColumn;

            this.moveCursorTo(newPos);
        }
    }

    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }

    public void moveCursorToEnd() {
        this.moveCursorTo(this.value.length());
    }

    private int getCursorPosX() {
        if (cursorLine < 0 || cursorLine >= textLines.size()) return 0;

        String lineText = textLines.get(cursorLine);
        int lineStart = getLineStartIndex(cursorLine);
        int relativePos = cursorPos - lineStart;

        if (relativePos < 0) relativePos = 0;
        if (relativePos > lineText.length()) relativePos = lineText.length();

        return font.width(lineText.substring(0, relativePos));
    }

    private int getCursorPos(int delta) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, delta);
    }

    public int getCursorPosition() {
        return this.cursorPos;
    }

    public int getWordPosition(int numWords) {
        return this.getWordPosition(numWords, this.getCursorPosition());
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private int nextWordBoundary(int pos) {
        int i = Mth.clamp(pos, 0, value.length());
        int len = value.length();
        while (i < len && Character.isWhitespace(value.charAt(i))) i++;
        if (i >= len) return len;
        if (isWordChar(value.charAt(i))) {
            while (i < len && isWordChar(value.charAt(i))) i++;
        } else {
            while (i < len && !Character.isWhitespace(value.charAt(i)) && !isWordChar(value.charAt(i))) i++;
        }
        return i;
    }

    private int prevWordBoundary(int pos) {
        int i = Mth.clamp(pos, 0, value.length());
        while (i > 0 && Character.isWhitespace(value.charAt(i - 1))) i--;
        if (i <= 0) return 0;
        if (isWordChar(value.charAt(i - 1))) {
            while (i > 0 && isWordChar(value.charAt(i - 1))) i--;
        } else {
            while (i > 0 && !Character.isWhitespace(value.charAt(i - 1)) && !isWordChar(value.charAt(i - 1))) i--;
        }
        return i;
    }

    private int getWordPosition(int n, int pos) {
        int i = pos;
        if (n == 0) return i;

        int steps = Math.abs(n);
        boolean backward = n < 0;
        for (int k = 0; k < steps; k++) {
            i = backward ? prevWordBoundary(i) : nextWordBoundary(i);
        }
        return i;
    }

    private int getLineStartIndex(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= lineStartIndices.size()) return 0;
        return lineStartIndices.get(lineIndex);
    }

    private int getLineEndIndex(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= lineStartIndices.size()) return 0;

        if (lineIndex == lineStartIndices.size() - 1) {
            return value.length();
        }
        return lineStartIndices.get(lineIndex + 1);
    }

    private int getLineVisualEndIndex(int lineIndex) {
        int start = getLineStartIndex(lineIndex);
        String lineText = (lineIndex >= 0 && lineIndex < textLines.size()) ? textLines.get(lineIndex) : "";
        return start + lineText.length();
    }

    private boolean isLineSelected(int lineIndex) {
        int minCursor = Math.min(cursorPos, selectionAnchor);
        int maxCursor = Math.max(cursorPos, selectionAnchor);
        if (minCursor == maxCursor) return false;

        int lineStart = getLineStartIndex(lineIndex);
        int lineEnd = getLineEndIndex(lineIndex);

        return minCursor < lineEnd && maxCursor > lineStart;
    }

    public String getHighlighted() {
        int i = Math.min(this.cursorPos, this.selectionAnchor);
        int j = Math.max(this.cursorPos, this.selectionAnchor);
        return this.value.substring(i, j);
    }

    // Only depends on focus/visibility
    public boolean canConsumeInput() {
        return this.visible && this.isFocused();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.canConsumeInput()) return false;

        this.shiftPressed = Screen.hasShiftDown();

        if (Screen.isCopy(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            return true;
        } else if (Screen.isSelectAll(keyCode)) {
            this.setSelectionAnchor(0);
            this.moveCursorToEnd();
            return true;
        } else if (Screen.isPaste(keyCode)) {
            if (!readOnly) {
                this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            }
            return true;
        } else if (Screen.isCut(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            if (!readOnly) {
                this.insertText("");
            }
            return true;
        }

        if (readOnly) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_PAGE_UP -> {
                    setScrollAmount(getScrollAmount() - getLineHeightScaled() * 4);
                    return true;
                }
                case GLFW.GLFW_KEY_PAGE_DOWN -> {
                    setScrollAmount(getScrollAmount() + getLineHeightScaled() * 4);
                    return true;
                }
                case GLFW.GLFW_KEY_UP -> {
                    setScrollAmount(getScrollAmount() - getLineHeightScaled());
                    return true;
                }
                case GLFW.GLFW_KEY_DOWN -> {
                    setScrollAmount(getScrollAmount() + getLineHeightScaled());
                    return true;
                }
                case GLFW.GLFW_KEY_HOME -> {
                    setScrollAmount(0);
                    return true;
                }
                case GLFW.GLFW_KEY_END -> {
                    setScrollAmount(this.maxScrollOffset);
                    return true;
                }
                default -> {
                    return false;
                }
            }
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER, 335 -> {
                if (!readOnly) {
                    this.insertText("\n");
                }
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (!readOnly) {
                    this.shiftPressed = false;
                    this.deleteText(-1);
                    this.shiftPressed = Screen.hasShiftDown();
                }
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (!readOnly) {
                    this.shiftPressed = false;
                    this.deleteText(1);
                    this.shiftPressed = Screen.hasShiftDown();
                }
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (Screen.hasControlDown()) {
                    this.moveCursorTo(this.getWordPosition(1));
                } else {
                    this.moveCursor(1);
                }
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                if (Screen.hasControlDown()) {
                    this.moveCursorTo(this.getWordPosition(-1));
                } else {
                    this.moveCursor(-1);
                }
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                this.moveCursorVertical(1);
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                this.moveCursorVertical(-1);
                return true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                if (Screen.hasControlDown()) {
                    this.moveCursorToStart();
                } else {
                    calculateLines();
                    updateCursorCoordinates();
                    int start = getLineStartIndex(cursorLine);
                    this.moveCursorTo(start);
                }
                return true;
            }
            case GLFW.GLFW_KEY_END -> {
                if (Screen.hasControlDown()) {
                    this.moveCursorToEnd();
                } else {
                    calculateLines();
                    updateCursorCoordinates();
                    int end = getLineVisualEndIndex(cursorLine);
                    this.moveCursorTo(end);
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public void playDownSound(SoundManager handler) {
        // No click sound
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.selectionAnchor = this.cursorPos;
            this.shiftPressed = false;
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!this.canConsumeInput()) return false;
        if (McVersionUtils.isAllowedChatCharacter(codePoint)) {
            if (!readOnly) {
                this.insertText(Character.toString(codePoint));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible || !this.isValidClickButton(button)) return false;

        if (isScrollbarHovered(mouseX, mouseY)) {
            // Capture drag by letting superclass register the click
            boolean consumed = super.mouseClicked(mouseX, mouseY, button);
            this.scrolling = true;
            this.followCaret = false; // manual scroll
            this.lastScrollingCall = System.currentTimeMillis();

            // Determine grab offset so the thumb doesn't jump
            int trackY = this.getY() + paddingY;
            int trackHeight = this.height - paddingY * 2;
            int contentHeightUnscaled = this.textLines.size() * getLineHeight();
            int visibleHeightUnscaled = (int) ((this.height - (paddingY * 2)) / textScale);
            int thumbHeight = Math.max(
                    16,
                    Math.min(
                            trackHeight,
                            (int) Math.round((double) visibleHeightUnscaled / (double) contentHeightUnscaled * trackHeight)
                    )
            );
            int currentThumbY;
            if (this.maxScrollOffset > 0) {
                double ratio = (double) this.scrollOffset / (double) this.maxScrollOffset;
                currentThumbY = trackY + (int) Math.round((trackHeight - thumbHeight) * ratio);
            } else {
                currentThumbY = trackY;
            }
            int offset = (int) Math.round(mouseY) - currentThumbY;
            // Clamp within thumb
            this.thumbGrabOffset = Mth.clamp(offset, 0, thumbHeight);

            return true;
        }

        // Focus the widget so wheel works; avoid selection change in read-only
        this.shiftPressed = readOnly ? false : Screen.hasShiftDown();
        if (!readOnly && !this.shiftPressed) {
            this.setSelectionAnchor(this.cursorPos);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // Only handle caret positioning when not read-only
        if (readOnly) {
            return;
        }

        if (mouseX >= this.getX() + paddingX && mouseX < this.getX() + this.width - paddingX - scrollBarWidth &&
                mouseY >= this.getY() + paddingY && mouseY < this.getY() + this.height - paddingY) {

            //---VR keyboard (only when not read-only)
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

            // Click positions caret; follow caret here
            followCaret = true;

            //---Click logic
            calculateLines();

            double relativeX = (mouseX - (getX() + paddingX)) / textScale;
            double relativeY = (mouseY - (getY() + paddingY)) / textScale + scrollOffset;
            int lineIndex = Mth.floor(relativeY / getLineHeight());

            if (lineIndex >= 0 && lineIndex < textLines.size()) {
                String lineText = textLines.get(lineIndex);
                int lineStart = lineStartIndices.get(lineIndex);

                if (lineText.isEmpty() || lineText.trim().isEmpty()) {
                    moveCursorTo(lineStart);
                } else {
                    int charPos = findClosestCharPosition(lineText, relativeX);
                    int newCursorPos = lineStart + charPos;
                    moveCursorTo(newCursorPos);
                }
            } else if (lineIndex >= textLines.size()) {
                moveCursorToEnd();
            }
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // Any mouse drag is a manual scroll/selection gesture: do not auto-follow caret
        followCaret = false;

        // Text selection drag (only when not read-only and not on scrollbar)
        if (button == 0 && !this.scrolling && !readOnly) {
            calculateLines();

            if (mouseY < this.getY() + paddingY) {
                setScrollAmount(getScrollAmount() - getLineHeightScaled());
            } else if (mouseY > this.getY() + this.height - paddingY) {
                setScrollAmount(getScrollAmount() + getLineHeightScaled());
            }

            double relativeX = (mouseX - (this.getX() + paddingX)) / textScale;
            double relativeY = (mouseY - (this.getY() + paddingY)) / textScale + scrollOffset;
            int lineIndex = Mth.floor(relativeY / getLineHeight());

            if (lineIndex < 0) {
                this.setCursorPositionKeepAnchor(0);
            } else if (lineIndex >= textLines.size()) {
                this.setCursorPositionKeepAnchor(value.length());
            } else {
                String lineText = textLines.get(lineIndex);
                int lineStart = lineStartIndices.get(lineIndex);
                if (lineText.isEmpty() || lineText.trim().isEmpty()) {
                    this.setCursorPositionKeepAnchor(lineStart);
                } else {
                    int charPos = findClosestCharPosition(lineText, relativeX);
                    this.setCursorPositionKeepAnchor(lineStart + charPos);
                }
            }
            return true;
        }

        // Scrollbar drag: only when drag started on scrollbar
        if (button == 0 && this.scrolling) {
            lastScrollingCall = System.currentTimeMillis();

            int trackY = this.getY() + paddingY;
            int trackHeight = this.height - paddingY * 2;

            int contentHeightUnscaled = this.textLines.size() * getLineHeight();
            int visibleHeightUnscaled = (int) ((this.height - (paddingY * 2)) / textScale);

            int thumbHeight = Math.max(
                    16,
                    Math.min(
                            trackHeight,
                            (int) Math.round((double) visibleHeightUnscaled / (double) contentHeightUnscaled * trackHeight)
                    )
            );

            double thumbTravel = Math.max(0.0, trackHeight - thumbHeight);
            double grab = (thumbGrabOffset >= 0) ? thumbGrabOffset : (thumbHeight / 2.0);
            double thumbPos = Mth.clamp(mouseY - trackY - grab, 0.0, thumbTravel);
            double ratio = (thumbTravel <= 0.0) ? 0.0 : (thumbPos / thumbTravel);
            this.setScrollAmount(ratio * this.maxScrollOffset);

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (this.isHoveredOrFocused() && this.visible) {
            // Manual scroll -> stop following caret
            followCaret = false;
            this.setScrollAmount(this.getScrollAmount() - scrollDelta * (double) getLineHeightScaled() / 2.0);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.scrolling = false;
            this.thumbGrabOffset = -1;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private int findClosestCharPosition(String text, double relativeX) {
        if (text.isEmpty()) return 0;

        int clickX = (int) relativeX;

        if (clickX <= 0) return 0;
        if (clickX >= font.width(text)) return text.length();

        int low = 0;
        int high = text.length();

        while (low < high) {
            int mid = (low + high) / 2;
            int width = font.width(text.substring(0, mid));

            if (width < clickX) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }

        if (low > 0) {
            int beforeWidth = font.width(text.substring(0, low - 1));
            int atWidth = font.width(text.substring(0, low));

            if (clickX - beforeWidth < atWidth - clickX) {
                return low - 1;
            }
        }

        return low;
    }

    protected boolean isScrollbarHovered(double mouseX, double mouseY) {
        int scrollbarX = getScrollbarX();
        return mouseX >= scrollbarX && mouseX < scrollbarX + scrollBarWidth &&
                mouseY >= this.getY() && mouseY < this.getY() + this.height;
    }

    protected int getScrollbarX() {
        return this.getX() + this.width - scrollBarWidth;
    }

    protected int getLineHeightScaled() {
        return (int) (getLineHeight() * textScale);
    }

    protected int getLineHeight() {
        return font.lineHeight + LINE_PADDING;
    }

    public double getScrollAmount() {
        return this.scrollOffset;
    }

    public void setScrollAmount(double amount) {
        calculateLines();
        this.scrollOffset = (int) Mth.clamp(amount, 0.0D, this.maxScrollOffset);
    }

    public void setSelectionAnchor(int position) {
        this.selectionAnchor = Mth.clamp(position, 0, this.value.length());
        updateCursorCoordinates = true;
    }
    private void setCursorPositionKeepAnchor(int pos) {
        this.cursorPos = Mth.clamp(pos, 0, this.value.length());
        this.updateCursorCoordinates = true;
    }

    public boolean isTextSelected() {
        return selectionAnchor != cursorPos;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        if (readOnly) {
            followCaret = false;
            this.selectionAnchor = this.cursorPos;
        }
    }

    public void setMaxLength(int length) {
        this.maxLength = length;
        if (this.value.length() > length) {
            this.setValue(this.value.substring(0, length));
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }
}