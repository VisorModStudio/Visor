package org.vmstudio.visor.api.client.input;


import com.mojang.blaze3d.platform.InputConstants;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//? if >=26.3 {
import org.lwjgl.sdl.SDLKeyboard;
//?} else {
/*import org.lwjgl.glfw.GLFW;
*///?}
import org.vmstudio.visor.api.compatibility.mcversion.gui.McGuiUtils;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
//?}

import java.util.BitSet;
import java.util.HashMap;
import java.util.Locale;

public class InputHelper {
    //? if >=26.3 {
    public static final int MOD_SHIFT = InputConstants.MOD_SHIFT;
    //?} else {
    /*public static final int MOD_SHIFT = GLFW.GLFW_MOD_SHIFT;
    *///?}
    public static final int CURSOR_NORMAL = 0x34001;
    public static final int CURSOR_DISABLED = 0x34003;

    private static final BitSet heldKeys = new BitSet();

    private static final HashMap<Character, Integer> keyCodes = new HashMap<>();

    private static long windowHandle() {
        //? if >=1.21.9 {
        return Minecraft.getInstance().getWindow().handle();
        //?} else {
        /*return Minecraft.getInstance().getWindow().getWindow();
        *///?}
    }


    public static void grabOrReleaseMouse(int cursorMode, double x, double y) {
        //? if >=26.3 {
        if (cursorMode == CURSOR_DISABLED) {
            InputConstants.grabMouse(Minecraft.getInstance().getWindow(), x, y);
        } else {
            InputConstants.releaseMouse(Minecraft.getInstance().getWindow(), x, y);
        }
        //?} elif >=1.21.9 {
        /*InputConstants.grabOrReleaseMouse(Minecraft.getInstance().getWindow(), cursorMode, x, y);
        *///?} else {
        /*InputConstants.grabOrReleaseMouse(windowHandle(), cursorMode, x, y);
        *///?}
    }


    /* ------- MOUSE ------- */

    public static void pressMouse(@NotNull MouseButtonType button, int modifiers) {
        //? if >=1.21.9 {
        Minecraft.getInstance().mouseHandler.onButton(
                windowHandle(), new MouseButtonInfo(button.getId(), modifiers), InputConstants.PRESS
        );
        //?} else {
        /*Minecraft.getInstance().mouseHandler.onPress(
                windowHandle(), button.getId(), InputConstants.PRESS, modifiers
        );
        *///?}
    }
    public static void pressMouse(@NotNull MouseButtonType button) {
        pressMouse(button, 0);
    }


    public static void releaseMouse(@NotNull MouseButtonType button, int modifiers) {
        //? if >=1.21.9 {
        Minecraft.getInstance().mouseHandler.onButton(
                windowHandle(), new MouseButtonInfo(button.getId(), modifiers), InputConstants.RELEASE
        );
        //?} else {
        /*Minecraft.getInstance().mouseHandler.onPress(
                windowHandle(), button.getId(), InputConstants.RELEASE, modifiers
        );
        *///?}
    }
    public static void releaseMouse(@NotNull MouseButtonType button) {
        releaseMouse(button, 0);
    }

    public static boolean isMousePressed(@NotNull MouseButtonType button){
        var mouseHandler =  Minecraft.getInstance().mouseHandler;
        switch (button){
            case LEFT ->{
                return mouseHandler.isLeftPressed();
            }
            case RIGHT ->{
                return mouseHandler.isRightPressed();
            }
            case MIDDLE ->{
                return mouseHandler.isMiddlePressed();
            }
        }
        return false;
    }

    public static void setMousePos(double x, double y) {
        //? if >=26.3 {
        var mouseHandler = Minecraft.getInstance().mouseHandler;
        mouseHandler.onMove(windowHandle(), x, y, x - mouseHandler.xpos(), y - mouseHandler.ypos());
        //?} else {
        /*Minecraft.getInstance().mouseHandler.onMove(windowHandle(), x, y);
        *///?}
    }


    public static void scrollMouse(double xOffset, double yOffset) {
        Minecraft.getInstance().mouseHandler.onScroll(windowHandle(), xOffset, yOffset);
    }

    /* ------- KEYBOARD ------- */

    public static void pressKey(int key, int modifiers) {
        if (key < 0) return;
        heldKeys.set(key);
        //? if >=1.21.9 {
        Minecraft.getInstance().keyboardHandler.keyPress(
                windowHandle(), InputConstants.PRESS, keyEvent(key, modifiers)
        );
        //?} else {
        /*Minecraft.getInstance().keyboardHandler.keyPress(
                windowHandle(), key, 0, InputConstants.PRESS, modifiers
        );
        *///?}
    }
    public static void pressKey(int key) {
        pressKey(key, 0);
    }


    public static void releaseKey(int key, int modifiers) {
        if (key < 0) return;
        heldKeys.clear(key);
        //? if >=1.21.9 {
        Minecraft.getInstance().keyboardHandler.keyPress(
                windowHandle(), InputConstants.RELEASE, keyEvent(key, modifiers)
        );
        //?} else {
        /*Minecraft.getInstance().keyboardHandler.keyPress(
                windowHandle(), key, 0, InputConstants.RELEASE, modifiers
        );
        *///?}
    }
    public static void releaseKey(int key) {
        releaseKey(key, 0);
    }


    //? if >=1.21.9 {
    private static KeyEvent keyEvent(int key, int modifiers) {
        //? if >=26.3 {
        return new KeyEvent(key, SDLKeyboard.SDL_GetKeyFromScancode(key, (short) modifiers, true), modifiers);
        //?} else {
        /*return new KeyEvent(key, 0, modifiers);
        *///?}
    }
    //?}

    public static boolean isKeyDown(int key) {
        if (key < 0) return false;
        //? if >=26.3 {
        return heldKeys.get(key) || InputConstants.isKeyDown(key);
        //?} else {
        /*return heldKeys.get(key)
                || GLFW.glfwGetKey(windowHandle(), key) == GLFW.GLFW_PRESS;
        *///?}
    }
    public static boolean isKeyDown(InputConstants.Key key) {
        //? if >=26.3 {
        return key.getType() == InputConstants.Type.KEYBOARD
                && key.getValue() != InputConstants.UNKNOWN.getValue()
                && isKeyDown(key.getValue());
        //?} else {
        /*return key.getType() == InputConstants.Type.KEYSYM
                && key.getValue() != GLFW.GLFW_KEY_UNKNOWN
                && isKeyDown(key.getValue());
        *///?}
    }

    public static boolean isVirtualKeyDown(int key) {
        return key >= 0 && heldKeys.get(key);
    }




    /* ------- KEYBOARD TEXT ------- */

    public static int getKeyCode(char character) {
        return keyCodes.getOrDefault(
                Character.toUpperCase(character),
                -1
        );
    }



    public static int getKeyCode(@Nullable String name) {
        if (name == null || name.isBlank()) return -1;

        String normalized = name.trim()
                .toLowerCase(Locale.ROOT)
                .replace(' ', '_');
        if (normalized.length() == 1) {
            return getKeyCode(normalized.charAt(0));
        }
        try {
            InputConstants.Key key = InputConstants.getKey(
                    "key.keyboard." + normalized
            );
            //? if >=26.3 {
            return key.getType() == InputConstants.Type.KEYBOARD
                    ? key.getValue()
                    : -1;
            //?} else {
            /*return key.getType() == InputConstants.Type.KEYSYM
                    ? key.getValue()
                    : -1;
            *///?}
        } catch (Exception e) {
            return -1;
        }
    }

    public static void pressChar(char character) {
        pressChar(character, 0);
    }
    public static void pressChar(char character, int modifiers) {
        pressKey(getKeyCode(character));
    }
    public static void releaseChar(char character) {
        releaseChar(character, 0);
    }
    public static void releaseChar(char character, int modifiers) {
        releaseKey(getKeyCode(character));
    }


    public static boolean sendChar(char character, int modifiers) {
        var keyboardAccessor = VisorAPI.client().getGuiManager()
                .getOverlayManager()
                .getKeyboardAccessor();
        Screen screen = keyboardAccessor.getAttachedTo();
        if(screen instanceof VROverlayScreen overlay){
            //overlays
            overlay.charTyped(character,modifiers);
            return true;
        }
        Minecraft mc = Minecraft.getInstance();
        if(McVersionClientUtils.screen() != null) {
            //? if >=1.21.9 {
            mc.keyboardHandler.charTyped(windowHandle(), McGuiUtils.characterEvent(character, modifiers));
            //?} else {
            /*mc.keyboardHandler.charTyped(windowHandle(), character, modifiers);
            *///?}
            return true;
        }
        return false;
    }

    public static void typeChar(char character, int modifiers) {
        if(sendChar(character, modifiers)) return;

        //keybindings
        int keyCode = getKeyCode(character);
        if(keyCode == -1) return;
        pressKey(keyCode);
        releaseKey(keyCode);

    }
    public static void typeChar(char character) {
        typeChar(character, 0);
    }
    public static void typeChars(CharSequence characters) {
        characters.chars().forEach(c -> typeChar((char) c));
    }



    static {
        // GLFW gives digits and letters their ASCII values, SDL scancodes are ordered as on the keyboard
        int[] digits = {
                InputConstants.KEY_0, InputConstants.KEY_1, InputConstants.KEY_2, InputConstants.KEY_3,
                InputConstants.KEY_4, InputConstants.KEY_5, InputConstants.KEY_6, InputConstants.KEY_7,
                InputConstants.KEY_8, InputConstants.KEY_9
        };
        for (char c = '0'; c <= '9'; c++) {
            keyCodes.put(c, digits[c - '0']);
        }
        int[] letters = {
                InputConstants.KEY_A, InputConstants.KEY_B, InputConstants.KEY_C, InputConstants.KEY_D,
                InputConstants.KEY_E, InputConstants.KEY_F, InputConstants.KEY_G, InputConstants.KEY_H,
                InputConstants.KEY_I, InputConstants.KEY_J, InputConstants.KEY_K, InputConstants.KEY_L,
                InputConstants.KEY_M, InputConstants.KEY_N, InputConstants.KEY_O, InputConstants.KEY_P,
                InputConstants.KEY_Q, InputConstants.KEY_R, InputConstants.KEY_S, InputConstants.KEY_T,
                InputConstants.KEY_U, InputConstants.KEY_V, InputConstants.KEY_W, InputConstants.KEY_X,
                InputConstants.KEY_Y, InputConstants.KEY_Z
        };
        for (char c = 'A'; c <= 'Z'; c++) {
            keyCodes.put(c, letters[c - 'A']);
        }
        keyCodes.put('`', InputConstants.KEY_GRAVE);
        keyCodes.put('-', InputConstants.KEY_MINUS);
        keyCodes.put('=', InputConstants.KEY_EQUALS);
        keyCodes.put('[', InputConstants.KEY_LBRACKET);
        keyCodes.put(']', InputConstants.KEY_RBRACKET);
        keyCodes.put('\\', InputConstants.KEY_BACKSLASH);
        keyCodes.put(';', InputConstants.KEY_SEMICOLON);
        keyCodes.put('\'', InputConstants.KEY_APOSTROPHE);
        keyCodes.put(',', InputConstants.KEY_COMMA);
        keyCodes.put('.', InputConstants.KEY_PERIOD);
        keyCodes.put('/', InputConstants.KEY_SLASH);
    }

}
