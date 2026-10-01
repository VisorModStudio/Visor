package org.vmstudio.visor.api.client.input;


import com.mojang.blaze3d.platform.InputConstants;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McGuiUtils;
//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
//?}

import java.util.BitSet;
import java.util.HashMap;
import java.util.Locale;

public class InputHelper {
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
        //? if >=1.21.9 {
        InputConstants.grabOrReleaseMouse(Minecraft.getInstance().getWindow(), cursorMode, x, y);
        //?} else {
        /*InputConstants.grabOrReleaseMouse(windowHandle(), cursorMode, x, y);
        *///?}
    }


    /* ------- MOUSE ------- */

    public static void pressMouse(@NotNull MouseButtonType button, int modifiers) {
        //? if >=1.21.9 {
        Minecraft.getInstance().mouseHandler.onButton(
                windowHandle(), new MouseButtonInfo(button.getId(), modifiers), GLFW.GLFW_PRESS
        );
        //?} else {
        /*Minecraft.getInstance().mouseHandler.onPress(
                windowHandle(), button.getId(), GLFW.GLFW_PRESS, modifiers
        );
        *///?}
    }
    public static void pressMouse(@NotNull MouseButtonType button) {
        pressMouse(button, 0);
    }


    public static void releaseMouse(@NotNull MouseButtonType button, int modifiers) {
        //? if >=1.21.9 {
        Minecraft.getInstance().mouseHandler.onButton(
                windowHandle(), new MouseButtonInfo(button.getId(), modifiers), GLFW.GLFW_RELEASE
        );
        //?} else {
        /*Minecraft.getInstance().mouseHandler.onPress(
                windowHandle(), button.getId(), GLFW.GLFW_RELEASE, modifiers
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
        Minecraft.getInstance().mouseHandler.onMove(windowHandle(), x, y);
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
                windowHandle(), GLFW.GLFW_PRESS, new KeyEvent(key, 0, modifiers)
        );
        //?} else {
        /*Minecraft.getInstance().keyboardHandler.keyPress(
                windowHandle(), key, 0, GLFW.GLFW_PRESS, modifiers
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
                windowHandle(), GLFW.GLFW_RELEASE, new KeyEvent(key, 0, modifiers)
        );
        //?} else {
        /*Minecraft.getInstance().keyboardHandler.keyPress(
                windowHandle(), key, 0, GLFW.GLFW_RELEASE, modifiers
        );
        *///?}
    }
    public static void releaseKey(int key) {
        releaseKey(key, 0);
    }


    public static boolean isKeyDown(int key) {
        if (key < 0) return false;
        return heldKeys.get(key)
                || GLFW.glfwGetKey(windowHandle(), key) == GLFW.GLFW_PRESS;
    }
    public static boolean isKeyDown(InputConstants.Key key) {
        return key.getType() == InputConstants.Type.KEYSYM
                && key.getValue() != GLFW.GLFW_KEY_UNKNOWN
                && isKeyDown(key.getValue());
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
            return key.getType() == InputConstants.Type.KEYSYM
                    ? key.getValue()
                    : -1;
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
        if(mc.screen != null) {
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
        // GLFW digit and letter key codes are their ASCII values
        for (char c = '0'; c <= '9'; c++) {
            keyCodes.put(c, (int) c);
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            keyCodes.put(c, (int) c);
        }
        keyCodes.put('`', GLFW.GLFW_KEY_GRAVE_ACCENT);
        keyCodes.put('-', GLFW.GLFW_KEY_MINUS);
        keyCodes.put('=', GLFW.GLFW_KEY_EQUAL);
        keyCodes.put('[', GLFW.GLFW_KEY_LEFT_BRACKET);
        keyCodes.put(']', GLFW.GLFW_KEY_RIGHT_BRACKET);
        keyCodes.put('\\', GLFW.GLFW_KEY_BACKSLASH);
        keyCodes.put(';', GLFW.GLFW_KEY_SEMICOLON);
        keyCodes.put('\'', GLFW.GLFW_KEY_APOSTROPHE);
        keyCodes.put(',', GLFW.GLFW_KEY_COMMA);
        keyCodes.put('.', GLFW.GLFW_KEY_PERIOD);
        keyCodes.put('/', GLFW.GLFW_KEY_SLASH);
    }

}
