package me.phoenixra.visor.api.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoTextBoxEditable;
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

    private boolean isEditable = true;


    private final List<String> textLines = new ArrayList<>();
    private final List<Integer> lineStartIndices = new ArrayList<>();
    private int cursorLine = 0;
    private int cursorColumn = 0;
    private int cursorPos;
    private int selectionAnchor;
    private boolean updateCursorCoordinates = true;
    protected boolean recalculateLines = true;

    @Getter
    protected int scrollOffset = 0;
    protected int maxScrollOffset = 0;
    protected long lastScrollingCall = -1;
    protected boolean scrolling = false;


    private boolean shiftPressed;


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
        this.textColor = widgetInfo.getTextColor().toInt();
        this.textHintColor = widgetInfo.getTextHintColor().toInt();
        this.highlightColor = widgetInfo.getHighlightColor().toInt();
        this.textScale = widgetInfo.getTextScale();
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

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        calculateLines();
        if (scrolling && lastScrollingCall + 200 < System.currentTimeMillis()) {
            scrolling = false;
            lastScrollingCall = -1;
        }

        if(background != null){
            background.blit(
                    guiGraphics,
                    getX(), getY(), width, height
            );
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
            } else if (this.isFocused() && (this.frame / 6) % 2 == 0) {
                guiGraphics.drawString(this.font, "_", 0, lineY, this.textColor);
            }
        } else {
            if (updateCursorCoordinates) {
                updateCursorCoordinates();
            }

            for (int i = 0; i < textLines.size(); i++) {
                if (lineY + lineHeight >= 0 && lineY <= (textMaxY - textY) / textScale) {
                    String lineText = textLines.get(i);
                    FormattedCharSequence line = FormattedCharSequence.forward(lineText, Style.EMPTY);

                    guiGraphics.drawString(this.font, line, 0, lineY, this.textColor);

                    if (isLineSelected(i)) {
                        renderSelectionHighlight(guiGraphics, i, lineY, lineHeight);
                    }

                    if (this.isFocused() && (this.frame / 6) % 2 == 0 && i == cursorLine) {
                        int cursorX = getCursorPosX();
                        int lineEndIndex = getLineEndIndex(i);

                        boolean isCursorAtLineEnd = cursorPos == lineEndIndex;

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

    protected void renderScrollBar(@NotNull GuiGraphics guiGraphics){
        if (maxScrollOffset > 0) {
            int visibleHeight = this.height;
            int contentHeight = (int) ((this.textLines.size() * (getLineHeight())) * textScale);

            int thumbHeight = Math.max(32, (visibleHeight * visibleHeight) / contentHeight);
            thumbHeight = Math.min(thumbHeight, visibleHeight);

            int thumbY = this.getY() + (int)((visibleHeight - thumbHeight) * (double)this.scrollOffset / this.maxScrollOffset);

            GuiTexture scrollBarTex = scrolling
                    ? textureScrollBarActive
                    : textureScrollBar;

            if (scrollBarTex != null) {
                scrollBarTex.blit(
                        guiGraphics,
                        getScrollbarX(), thumbY,
                        scrollBarWidth, thumbHeight
                );
            }
        }
    }

    private void renderSelectionHighlight(GuiGraphics guiGraphics, int lineIndex, int lineY, int lineHeight) {
        int minCursor = Math.min(cursorPos, selectionAnchor);
        int maxCursor = Math.max(cursorPos, selectionAnchor);

        int lineStart = getLineStartIndex(lineIndex);
        int lineEnd   = getLineEndIndex(lineIndex);

        if (maxCursor <= lineStart || minCursor >= lineEnd) return;

        int selStart = Math.max(minCursor, lineStart) - lineStart;
        int selEnd   = Math.min(maxCursor, lineEnd)   - lineStart;

        String lineText = textLines.get(lineIndex);
        int textLen     = lineText.length();

        selStart = Mth.clamp(selStart, 0, textLen);
        selEnd   = Mth.clamp(selEnd,   0, textLen);

        int startX, endX;
        if (textLen == 0) {
            int spaceWidth = this.font.width(" ");
            startX = 0;
            endX   = spaceWidth;
        } else {
            startX = this.font.width(lineText.substring(0, selStart));
            endX   = this.font.width(lineText.substring(0, selEnd));
        }

        var halfPadding = LINE_PADDING/2;
        guiGraphics.fill(
                RenderType.guiTextHighlight(),
                startX,              // x0
                lineY - halfPadding , // y0
                endX,                 // x1
                lineY + halfPadding + font.lineHeight,  // y1
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
            this.scrollOffset    = 0;
            return;
        }

        String[] explicitLines = value.split("\n", -1);
        int currentPos = 0;

        for (int i = 0; i < explicitLines.length; i++) {
            String explicitLine = explicitLines[i];

            if (i > 0) {
                currentPos++;
            }
            if (explicitLine.isEmpty()) {
                textLines.add("");
                lineStartIndices.add(currentPos);
                continue;
            }

            List<String> tokens = new ArrayList<>();
            Matcher m = Pattern.compile("\\s+|\\S+").matcher(explicitLine);
            while (m.find()) {
                tokens.add(m.group());
            }

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
                            sb.append(fit);
                            currentPos += fit.length();
                            tokens.set(tokenIdx, tok.substring(fit.length()));
                        }
                        break;
                    }
                }

                textLines.add(sb.toString());
            }
        }

        int scaledVisibleHeight = (int) ((this.height - (paddingY * 2)) / textScale);
        int totalTextHeight = this.textLines.size() * getLineHeight();
        this.maxScrollOffset = Math.max(0, totalTextHeight - scaledVisibleHeight);
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

        ensureCursorVisible();
        updateCursorCoordinates = false;
    }

    private void ensureCursorVisible() {
        if(isTextSelected()){
            return;
        }
        int lineHeight = getLineHeight();
        int cursorY = cursorLine * lineHeight;

        int visibleTop = scrollOffset;
        int visibleBottom = scrollOffset + (int)((height - (paddingY * 2)) / textScale);

        if (cursorY < visibleTop) {
            scrollOffset = cursorY;
        } else if (cursorY + lineHeight > visibleBottom) {
            scrollOffset = cursorY - (int)((height - (paddingY * 2)) / textScale) + lineHeight;
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
            this.onValueChange(text);
        }
    }

    public void insertText(String textToWrite) {
        int i = Math.min(this.cursorPos, this.selectionAnchor);
        int j = Math.max(this.cursorPos, this.selectionAnchor);
        int k = this.maxLength - this.value.length() - (i - j);
        if (k <= 0) return;

        String string = SharedConstants.filterText(textToWrite, true);
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
        if (Screen.hasControlDown()) {
            this.deleteWords(count);
        } else {
            this.deleteChars(count);
        }
    }

    public void deleteWords(int num) {
        if (!this.value.isEmpty()) {
            if (this.selectionAnchor != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(num) - this.cursorPos);
            }
        }
    }

    public void deleteChars(int num) {
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
                        this.onValueChange(value);
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

    private int getWordPosition(int n, int pos) {
        return this.getWordPosition(n, pos, true);
    }

    private int getWordPosition(int n, int pos, boolean skipWs) {
        int i = pos;
        boolean bl = n < 0;
        int j = Math.abs(n);

        for(int k = 0; k < j; ++k) {
            if (!bl) {
                int l = this.value.length();
                i = this.value.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while(skipWs && i < l && this.value.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while(skipWs && i > 0 && this.value.charAt(i - 1) == ' ') {
                    --i;
                }

                while(i > 0 && this.value.charAt(i - 1) != ' ') {
                    --i;
                }
            }
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


    public boolean canConsumeInput() {
        return this.visible && this.isFocused() && this.isEditable;
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.canConsumeInput()) {
            return false;
        }

        this.shiftPressed = Screen.hasShiftDown();

        if (Screen.isSelectAll(keyCode)) {
            this.moveCursorToEnd();
            this.setSelectionAnchor(0);
            return true;
        } else if (Screen.isCopy(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            return true;
        } else if (Screen.isPaste(keyCode)) {
            if (this.isEditable) {
                this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            }
            return true;
        } else if (Screen.isCut(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            if (this.isEditable) {
                this.insertText("");
            }
            return true;
        }

        return switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER, 335 -> {
                if (this.isEditable) {
                    this.insertText("\n");
                }
                yield true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (this.isEditable) {
                    this.shiftPressed = false;
                    this.deleteText(-1);
                    this.shiftPressed = Screen.hasShiftDown();
                }
                yield true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (this.isEditable) {
                    this.shiftPressed = false;
                    this.deleteText(1);
                    this.shiftPressed = Screen.hasShiftDown();
                }
                yield true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (Screen.hasControlDown()) {
                    this.moveCursorTo(this.getWordPosition(1));
                } else {
                    this.moveCursor(1);
                }
                yield true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                if (Screen.hasControlDown()) {
                    this.moveCursorTo(this.getWordPosition(-1));
                } else {
                    this.moveCursor(-1);
                }
                yield true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                this.moveCursorVertical(1);
                yield true;
            }
            case GLFW.GLFW_KEY_UP -> {
                this.moveCursorVertical(-1);
                yield true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                this.moveCursorToStart();
                yield true;
            }
            case GLFW.GLFW_KEY_END -> {
                this.moveCursorToEnd();
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public void playDownSound(SoundManager handler) {

    }

    @Override
    public void setFocused(boolean focused) {
        if(!isEditable){
            return;
        }
        super.setFocused(focused);
        if (!focused) {
            this.selectionAnchor = this.cursorPos;
            this.shiftPressed = false;
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!this.canConsumeInput()) {
            return false;
        } else if (SharedConstants.isAllowedChatCharacter(codePoint)) {
            if (this.isEditable) {
                this.insertText(Character.toString(codePoint));
            }
            return true;
        } else {
            return false;
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active
                || !this.visible
                || !this.isValidClickButton(button)) return false;

        if (isScrollbarHovered(mouseX, mouseY)) {
            this.scrolling = true;
            lastScrollingCall = System.currentTimeMillis();
            return true;
        }

        if(isEditable) {
            this.shiftPressed = Screen.hasShiftDown();
            if (!this.shiftPressed) {
                this.setSelectionAnchor(this.cursorPos);
            }
        }

        return isEditable && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {

        if (mouseX >= this.getX() + paddingX && mouseX < this.getX() + this.width - paddingX - scrollBarWidth &&
                mouseY >= this.getY() + paddingY && mouseY < this.getY() + this.height - paddingY) {

            //---VR keyboard
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
                    keyboardAccessor.showKeyboard(
                            screenFocused
                    );
                }
            }

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
        if (button == 0 && !this.scrolling && isEditable) {
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
                this.setSelectionAnchor(0);
            } else if (lineIndex >= textLines.size()) {
                this.setSelectionAnchor(value.length());
            } else {
                String lineText = textLines.get(lineIndex);
                int lineStart = lineStartIndices.get(lineIndex);
                if (lineText.isEmpty() || lineText.trim().isEmpty()) {
                    this.setSelectionAnchor(lineStart);
                } else {
                    int charPos = findClosestCharPosition(lineText, relativeX);
                    this.setSelectionAnchor(lineStart + charPos);
                }
            }
            return true;
        }

        if (button == 0) {
            lastScrollingCall = System.currentTimeMillis();

            if (mouseY < this.getY()) {
                this.setScrollAmount(0);
            } else if (mouseY > this.getY() + this.height) {
                this.setScrollAmount(this.maxScrollOffset);
            } else {
                double visibleHeight = this.height - (paddingY * 2);
                int thumbHeight = (int)Math.max(
                        32,
                        visibleHeight * visibleHeight / (this.textLines.size() * getLineHeightScaled())
                );
                double scrollFactor = Math.max(
                        1.0,
                        (double)this.maxScrollOffset / (double)(this.height - thumbHeight)
                );
                this.setScrollAmount(this.getScrollAmount() + dragY * scrollFactor);
            }
            return true;
        }

        return false;
    }



    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (this.isHoveredOrFocused() && this.visible) {
            this.setScrollAmount(this.getScrollAmount() - scrollDelta * (double) getLineHeightScaled() / 2.0);
            return true;
        }
        return false;
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.scrolling = false;
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
        return this.getX() + this.width - scrollBarWidth - 1;
    }

    protected int getLineHeightScaled() {
        return (int)(getLineHeight() * textScale);
    }
    protected int getLineHeight() {
        return font.lineHeight + LINE_PADDING;
    }

    public double getScrollAmount() {
        return this.scrollOffset;
    }

    public void setScrollAmount(double amount) {
        calculateLines();
        this.scrollOffset = (int)Mth.clamp(amount, 0.0D, this.maxScrollOffset);
    }

    public void setSelectionAnchor(int position) {
        this.selectionAnchor = Mth.clamp(position, 0, this.value.length());
        updateCursorCoordinates = true;
    }
    public boolean isTextSelected(){
        return selectionAnchor != cursorPos;
    }

    public void setEditable(boolean enabled) {
        this.isEditable = enabled;
    }

    public boolean isEditable() {
        return this.isEditable;
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