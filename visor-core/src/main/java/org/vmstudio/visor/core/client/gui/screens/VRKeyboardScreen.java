package org.vmstudio.visor.core.client.gui.screens;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard.KeyboardButton;
import org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard.KeyboardKey;
import org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard.KeyboardLayoutKeys;
import org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard.KeyboardLayouts;
import org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard.VROverlayKeyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VRKeyboardScreen extends Screen {
    @Getter
    @Setter
    private VROverlayKeyboard overlayKeyboard;
    @Getter
    @Setter
    private Runnable pressedTask;

    @Getter
    @Setter
    private int pressTick;

    private final Map<Integer, Integer> pressedKeys = new HashMap<>();

    @Getter
    private int cursorBoundsX = -1;
    @Getter
    private int cursorBoundsY = -1;
    @Getter
    private int cursorBoundsWidth = -1;
    @Getter
    private int cursorBoundsHeight = -1;

    public VRKeyboardScreen(Component component) {
        super(component);
    }

    @Override
    public void init() {
        this.clearWidgets();

        int gridStart = 32;
        int keyGap = 2;
        int keyWidth = 25;
        int keyHeight = 20;
        int smallButtonWidth = 30;
        int sideButtonWidth = 35;
        int shiftButtonWidth = 50;
        int languageButtonWidth = 38;
        int spaceX;

        KeyboardLayoutKeys layoutKeys = KeyboardLayouts.get(overlayKeyboard.getActiveLayout());
        KeyboardKey[][] rows = layoutKeys.getKeys(overlayKeyboard.isShiftPressed());
        int maxColumns = layoutKeys.getMaxColumns();
        int gridRightX = gridStart + maxColumns * (keyWidth + keyGap);

        for (int row = 0; row < rows.length; ++row) {
            KeyboardKey[] rowKeys = rows[row];
            int rowStartX = gridStart + ((maxColumns - rowKeys.length) * (keyWidth + keyGap)) / 2;
            int rowY = gridStart + row * (keyHeight + keyGap);

            for (int column = 0; column < rowKeys.length; ++column) {
                KeyboardKey key = rowKeys[column];
                KeyboardButton button = new KeyboardButton.Builder(
                        this,
                        Component.literal(key.getLabel()),
                        (p) -> pressKeyboardKey(key)
                ).size(keyWidth, keyHeight)
                        .pos(rowStartX + column * (keyWidth + keyGap), rowY)
                        .onRelease((p) -> releaseKeyboardKey(key))
                        .build();
                this.addRenderableWidget(button);
            }
        }

        int bottomY = gridStart + rows.length * (keyHeight + keyGap);
        spaceX = gridStart + ((maxColumns - 5) / 2) * (keyWidth + keyGap);

        //SHIFT
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal(
                                overlayKeyboard.isShiftPressed()
                                        ? "SHIFT"
                                        : "Shift"
                        ),
                        (p) -> overlayKeyboard.setShiftPressed(!overlayKeyboard.isShiftPressed()))
                        .size(overlayKeyboard.isShiftPressed() ? (shiftButtonWidth + 2) : shiftButtonWidth, keyHeight)
                        .pos(0, gridStart + (rows.length - 1) * (keyHeight + keyGap))
                        .usePressTask(false)
                        .build()
        );
        //LANGUAGE
        KeyboardButton languageButton = new KeyboardButton.Builder(this,
                Component.literal(layoutKeys.getLayout().getLabel()),
                (p) -> overlayKeyboard.cycleLayout())
                .size(languageButtonWidth, keyHeight)
                .pos(3 * (sideButtonWidth + keyGap) + gridStart, gridStart - (keyHeight + keyGap))
                .usePressTask(false)
                .build();
        languageButton.active = overlayKeyboard.hasMultipleLayouts();
        this.addRenderableWidget(languageButton);
        //SPACE
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal(" "),
                        (p) -> pressSpace())
                        .size(5 * (keyWidth + keyGap), keyHeight)
                        .pos(spaceX, bottomY)
                        .onRelease((p) -> releaseKey(GLFW.GLFW_KEY_SPACE))
                        .build()
        );
        //BACKSPACE
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("BKSP"),
                        (p) -> pressKeyAction(GLFW.GLFW_KEY_BACKSPACE))
                        .size(sideButtonWidth, keyHeight)
                        .pos(gridRightX, gridStart)
                        .build()
        );
        //ENTER
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("ENTER"),
                        (p) -> pressKeyAction(GLFW.GLFW_KEY_ENTER))
                        .size(sideButtonWidth, keyHeight)
                        .pos(gridRightX, gridStart + 2 * (keyHeight + keyGap))
                        .usePressTask(false)
                        .build()
        );
        //TAB
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("TAB"),
                        (p) -> pressKeyAction(GLFW.GLFW_KEY_TAB))
                        .size(smallButtonWidth, keyHeight)
                        .pos(0, gridStart + keyHeight + keyGap)
                        .usePressTask(false)
                        .build()
        );
        //CLOSE
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("§cx"),
                        (p) ->
                        {
                            var keyboardAccessor = ClientContext.overlayManager.getKeyboardAccessor();
                            keyboardAccessor.setVisible(false);
                        })
                        .size(smallButtonWidth, keyHeight)
                        .pos(0, gridStart - (keyHeight + keyGap))
                        .usePressTask(false)
                        .build()
        );
        //ESCAPE
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("ESC"),
                        (p) -> pressKeyAction(GLFW.GLFW_KEY_ESCAPE))
                        .size(smallButtonWidth, keyHeight)
                        .pos(0, gridStart)
                        .usePressTask(false)
                        .build()
        );
        //ARROW UP
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("↑"),
                        (p) -> pressNavigationKey(GLFW.GLFW_KEY_UP))
                        .size(keyWidth, keyHeight)
                        .pos((maxColumns - 1) * (keyWidth + keyGap) + gridStart, bottomY)
                        .build()
        );
        //ARROW DOWN
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("↓"),
                        (p) -> pressNavigationKey(GLFW.GLFW_KEY_DOWN))
                        .size(keyWidth, keyHeight)
                        .pos((maxColumns - 1) * (keyWidth + keyGap) + gridStart, bottomY + keyHeight + keyGap)
                        .build()
        );
        //ARROW LEFT
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("←"),
                        (p) -> pressNavigationKey(GLFW.GLFW_KEY_LEFT))
                        .size(keyWidth, keyHeight)
                        .pos((maxColumns - 2) * (keyWidth + keyGap) + gridStart, bottomY + keyHeight + keyGap)
                        .build()
        );
        //ARROW RIGHT
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("→"),
                        (p) -> pressNavigationKey(GLFW.GLFW_KEY_RIGHT))
                        .size(keyWidth, keyHeight)
                        .pos(maxColumns * (keyWidth + keyGap) + gridStart, bottomY + keyHeight + keyGap)
                        .build()
        );
        //CUT
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("CUT"),
                        (p) ->
                        {
                            InputHelper.pressKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                            InputHelper.pressKey(GLFW.GLFW_KEY_X);
                            InputHelper.releaseKey(GLFW.GLFW_KEY_X);
                            InputHelper.releaseKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                        })
                        .size(sideButtonWidth, keyHeight)
                        .pos(gridStart, gridStart - (keyHeight + keyGap))
                        .usePressTask(false)
                        .build()
        );
        //COPY
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("COPY"),
                        (p) ->
                        {
                            InputHelper.pressKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                            InputHelper.pressKey(GLFW.GLFW_KEY_C);
                            InputHelper.releaseKey(GLFW.GLFW_KEY_C);
                            InputHelper.releaseKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                        })
                        .size(sideButtonWidth, keyHeight)
                        .pos(sideButtonWidth + keyGap + gridStart, gridStart - (keyHeight + keyGap))
                        .usePressTask(false)
                        .build()
        );
        //PASTE
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("PASTE"),
                        (p) ->
                        {
                            InputHelper.pressKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                            InputHelper.pressKey(GLFW.GLFW_KEY_V);
                            InputHelper.releaseKey(GLFW.GLFW_KEY_V);
                            InputHelper.releaseKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                        })
                        .size(sideButtonWidth, keyHeight)
                        .pos(2 * (sideButtonWidth + keyGap) + gridStart, gridStart - (keyHeight + keyGap))
                        .usePressTask(false)
                        .build()
        );
        //CURSOR BOUNDS
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (var child : this.children()) {
            if (child instanceof AbstractWidget widget) {
                minX = Math.min(minX, widget.getX());
                minY = Math.min(minY, widget.getY());
                maxX = Math.max(maxX, widget.getX() + widget.getWidth());
                maxY = Math.max(maxY, widget.getY() + widget.getHeight());
            }
        }
        if (minX == Integer.MAX_VALUE) {
            cursorBoundsX = -1;
            cursorBoundsY = -1;
            cursorBoundsWidth = -1;
            cursorBoundsHeight = -1;
        } else {
            cursorBoundsX = minX;
            cursorBoundsY = minY;
            cursorBoundsWidth = maxX - minX;
            cursorBoundsHeight = maxY - minY;
        }
    }

    private void pressKeyboardKey(KeyboardKey key) {
        if (canTypeText()) {
            InputHelper.typeChars(key.getInput());
            return;
        }
        if (!key.hasFallback()) {
            return;
        }

        pressKey(key.getFallbackKey(), key.getFallbackModifiers());
    }

    private void releaseKeyboardKey(KeyboardKey key) {
        if (key.hasFallback()) {
            releaseKey(key.getFallbackKey());
        }
    }

    private void pressSpace() {
        if (canTypeText()) {
            InputHelper.typeChars(" ");
            return;
        }

        pressKey(GLFW.GLFW_KEY_SPACE, 0);
    }

    private void pressNavigationKey(int key) {
        int modifiers = overlayKeyboard.isShiftPressed()
                ? GLFW.GLFW_MOD_SHIFT
                : 0;
        pressKeyAction(key, modifiers);
    }

    private void pressKey(int key, int modifiers) {
        if (pressedKeys.containsKey(key)) {
            return;
        }
        pressedTask = null;
        pressTick = 0;

        pressedKeys.put(key, modifiers);
        pressModifiers(modifiers);
        InputHelper.pressKey(key, modifiers);
    }

    private void releaseKey(int key) {
        Integer modifiers = pressedKeys.remove(key);
        if (modifiers == null) {
            return;
        }
        InputHelper.releaseKey(key, modifiers);
        releaseModifiers(modifiers);
    }

    private void releaseKeys() {
        if (pressedKeys.isEmpty()) {
            return;
        }
        for (int key : new ArrayList<>(pressedKeys.keySet())) {
            releaseKey(key);
        }
    }

    private void pressKeyAction(int key) {
        pressKeyAction(key, 0);
    }

    private void pressKeyAction(int key,
                                int temporaryModifiers) {
        pressModifiers(temporaryModifiers);
        InputHelper.pressKey(key, temporaryModifiers);
        InputHelper.releaseKey(key, temporaryModifiers);
        releaseModifiers(temporaryModifiers);
    }

    private boolean canTypeText() {
        return overlayKeyboard.getAttachedTo() != null
                || Minecraft.getInstance().screen != null;
    }

    private void pressModifiers(int modifiers) {
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
            InputHelper.pressKey(GLFW.GLFW_KEY_LEFT_SHIFT);
        }
    }

    private void releaseModifiers(int modifiers) {
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
            InputHelper.releaseKey(GLFW.GLFW_KEY_LEFT_SHIFT);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {

    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        clearPress();
        return super.mouseReleased(d, e, i);
    }

    public void clearPress() {
        pressedTask = null;
        pressTick = 0;
        releaseKeys();
    }

    @Override
    public void tick() {
        super.tick();
        if (pressedTask != null) {
            if (pressTick < 20) {
                pressTick++;
                return;
            }
            pressedTask.run();
        }
    }
}
