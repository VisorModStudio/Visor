package me.phoenixra.visor.core.client.gui.screens;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.keyboard.KeyboardButton;
import me.phoenixra.visor.core.client.gui.overlays.builtin.keyboard.VROverlayKeyboard;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;


public class VRKeyboardScreen extends Screen {
    @Getter @Setter
    private VROverlayKeyboard overlayKeyboard;
    @Getter @Setter
    private Runnable pressedTask;

    @Getter @Setter
    private int pressTick;

    public VRKeyboardScreen(Component component) {
        super(component);
    }

    @Override
    public void init() {
        String keys = VRClientSettings.getKeyboardKeys();
        String keysShift = VRClientSettings.getKeyboardKeysShift();
        this.clearWidgets();

        if (overlayKeyboard.isShiftPressed()) {
            keys = keysShift;
        }

        int keysPerRow = 13;
        int rows;
        int yPos = 32;
        int l = 2;
        int i1 = 25;
        double preRows = (double) keys.length() / (double) keysPerRow;

        if (Math.floor(preRows) == preRows) {
            rows = (int) preRows;
        } else {
            rows = (int) (preRows + 1.0D);
        }

        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < keysPerRow; ++column) {
                int index = row * keysPerRow + column;
                char keyChar = ' ';

                if (index < keys.length()) {
                    keyChar = keys.charAt(index);
                }

                String label = String.valueOf(keyChar);
                KeyboardButton button = new KeyboardButton.Builder(
                        this,
                        Component.literal(label), (p) ->
                {
                    InputHelper.typeChars(label);
                })
                        .size(i1, 20)
                        .pos(yPos + column * (i1 + l), yPos + row * (20 + l))
                        .build();
                this.addRenderableWidget(button);
            }
        }

        //SHIFT
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal(overlayKeyboard.isShiftPressed()
                                ? "SHIFT"
                                : "Shift"),
                        (p) ->
                        {
                            overlayKeyboard
                                    .setShiftPressed(!overlayKeyboard.isShiftPressed());
                        })
                        .size(overlayKeyboard.isShiftPressed() ? 32 : 30, 20)
                        .pos(0, yPos + 3 * (20 + l))
                        .usePressTask(false)
                        .build()
        );
        //SPACE
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal(" "),
                        (p) ->
                        {
                            InputHelper.typeChars(" ");
                        })
                        .size(5 * (i1 + l), 20)
                        .pos(yPos + 4 * (i1 + l), yPos + rows * (20 + l))
                        .build()
        );
        //BACKSPACE
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("BKSP"),
                        (p) ->
                        {
                            InputHelper.pressKey(GLFW.GLFW_KEY_BACKSPACE);
                            InputHelper.releaseKey(GLFW.GLFW_KEY_BACKSPACE);
                        })
                        .size(35, 20)
                        .pos(keysPerRow * (i1 + l) + yPos, yPos)
                        .build()
        );
        //ENTER
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("ENTER"),
                        (p) ->
                        {
                            InputHelper.pressKey(GLFW.GLFW_KEY_ENTER);
                            InputHelper.releaseKey(GLFW.GLFW_KEY_ENTER);
                        })
                        .size(35, 20)
                        .pos(keysPerRow * (i1 + l) + yPos, yPos + 2 * (20 + l))
                        .usePressTask(false)
                        .build()
        );
        //TAB
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("TAB"),
                        (p) ->
                        {
                            InputHelper.pressKey(GLFW.GLFW_KEY_TAB);
                            InputHelper.releaseKey(GLFW.GLFW_KEY_TAB);
                        })
                        .size(30, 20)
                        .pos(0, yPos + 20 + l)
                        .usePressTask(false)
                        .build()
        );

        //CLOSE
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("§cx"),
                        (p) ->
                        {
                            ClientContext.overlayManager
                                    .showKeyboard(false);
                        })
                        .size(30, 20)
                        .pos(0, yPos + -1 * (20 + l))
                        .usePressTask(false)
                        .build()
        );

        //ESCAPE
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("ESC"),
                        (p) ->
                        {
                            InputHelper.pressKey(GLFW.GLFW_KEY_ESCAPE);
                            InputHelper.releaseKey(GLFW.GLFW_KEY_ESCAPE);
                        })
                        .size(30, 20)
                        .pos(0, yPos)
                        .usePressTask(false)
                        .build()
        );
        //ARROW UP
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("\u2191"),
                        (p) ->
                        {
                            if(overlayKeyboard.isShiftPressed()){
                                InputHelper.pressKey(GLFW.GLFW_KEY_LEFT_SHIFT);
                            }

                            InputHelper.pressKey(GLFW.GLFW_KEY_UP);
                            InputHelper.releaseKey(GLFW.GLFW_KEY_UP);

                            InputHelper.releaseKey(GLFW.GLFW_KEY_LEFT_SHIFT);
                        })
                        .size(i1, 20)
                        .pos((keysPerRow - 1) * (i1 + l) + yPos, yPos + rows * (20 + l))
                        .build()
        );
        //ARROW DOWN
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("\u2193"),
                        (p) ->
                        {
                            if(overlayKeyboard.isShiftPressed()){
                                InputHelper.pressKey(GLFW.GLFW_KEY_LEFT_SHIFT);
                            }
                            InputHelper.pressKey(GLFW.GLFW_KEY_DOWN);
                            InputHelper.releaseKey(GLFW.GLFW_KEY_DOWN);

                            InputHelper.releaseKey(GLFW.GLFW_KEY_LEFT_SHIFT);
                        })
                        .size(i1, 20)
                        .pos((keysPerRow - 1) * (i1 + l) + yPos, yPos + (rows + 1) * (20 + l))
                        .build()
        );
        //ARROW LEFT
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("\u2190"),
                        (p) ->
                        {
                            if(overlayKeyboard.isShiftPressed()){
                                InputHelper.pressKey(GLFW.GLFW_KEY_LEFT_SHIFT);
                            }
                            InputHelper.pressKey(GLFW.GLFW_KEY_LEFT);
                            InputHelper.releaseKey(GLFW.GLFW_KEY_LEFT);

                            InputHelper.releaseKey(GLFW.GLFW_KEY_LEFT_SHIFT);
                        })
                        .size(i1, 20)
                        .pos((keysPerRow - 2) * (i1 + l) + yPos, yPos + (rows + 1) * (20 + l))
                        .build()
        );
        //ARROW RIGHT
        this.addRenderableWidget(
                new KeyboardButton.Builder(this,
                        Component.literal("\u2192"),
                        (p) ->
                        {
                            if(overlayKeyboard.isShiftPressed()){
                                InputHelper.pressKey(GLFW.GLFW_KEY_LEFT_SHIFT);
                            }
                            InputHelper.pressKey(GLFW.GLFW_KEY_RIGHT);
                            InputHelper.releaseKey(GLFW.GLFW_KEY_RIGHT);

                            InputHelper.releaseKey(GLFW.GLFW_KEY_LEFT_SHIFT);
                        })
                        .size(i1, 20)
                        .pos(keysPerRow * (i1 + l) + yPos, yPos + (rows + 1) * (20 + l))
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
                        .size(35, 20)
                        .pos(yPos, yPos + -1 * (20 + l))
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
                        .size(35, 20)
                        .pos(35 + l + yPos, yPos + -1 * (20 + l))
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
                        .size(35, 20)
                        .pos(2 * (35 + l) + yPos, yPos + -1 * (20 + l))
                        .usePressTask(false)
                        .build()
        );
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
