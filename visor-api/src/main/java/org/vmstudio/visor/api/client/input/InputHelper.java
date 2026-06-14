package org.vmstudio.visor.api.client.input;


import com.mojang.blaze3d.platform.InputConstants;
import org.vmstudio.visor.api.VisorAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class InputHelper {
    private static final Set<Integer> pressedKeys = new HashSet<>();

    private static final HashMap<Character, Integer> keyCodes = new HashMap<>();


    public static void pressMouse(@NotNull MouseButtonType button, int modifiers) {
        Minecraft.getInstance().mouseHandler.onPress(
                Minecraft.getInstance().getWindow().getWindow(),
                button.getId(), 1, modifiers
        );

    }
    public static void pressMouse(@NotNull MouseButtonType button) {
        pressMouse(button, 0);
    }


    public static void releaseMouse(@NotNull MouseButtonType button, int modifiers) {
        Minecraft.getInstance().mouseHandler.onPress(
                Minecraft.getInstance().getWindow().getWindow(),
                button.getId(), 0, modifiers
        );
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
        Minecraft.getInstance().mouseHandler.onMove(
                Minecraft.getInstance().getWindow().getWindow(),
                x, y
        );
    }


    public static void scrollMouse(double xOffset, double yOffset) {
        Minecraft.getInstance().mouseHandler.onScroll(
                Minecraft.getInstance().getWindow().getWindow(),
                xOffset, yOffset
        );
    }


    public static void pressKey(int key, int modifiers) {
        pressedKeys.add(key);
        Minecraft.getInstance().keyboardHandler.keyPress(
                Minecraft.getInstance().getWindow().getWindow(),
                key, 0, 1, modifiers
        );
    }
    public static void pressKey(int key) {
        pressKey(key, 0);
    }


    public static void releaseKey(int key, int modifiers) {
        pressedKeys.remove(key);
        Minecraft.getInstance().keyboardHandler.keyPress(
                Minecraft.getInstance().getWindow().getWindow(),
                key, 0, 0, modifiers
        );
    }
    public static void releaseKey(int key) {
        releaseKey(key, 0);
    }


    public static boolean isKeyDown(int key) {
        return pressedKeys.contains(key) || GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), key) == 1 ;
    }
    public static boolean isKeyDown(InputConstants.Key key) {
        return key.getType() == InputConstants.Type.KEYSYM
                && key.getValue() != GLFW.GLFW_KEY_UNKNOWN
                && isKeyDown(key.getValue());
    }

    public static void pressChar(char character) {
        pressChar(character, 0);
    }
    public static void pressChar(char character, int modifiers) {

        int keyCode = keyCodes.getOrDefault(
                Character.toUpperCase(character),
                -1
        );
        if(keyCode == -1) return;
        pressKey(keyCode);
    }
    public static void releaseChar(char character) {
        releaseChar(character, 0);
    }
    public static void releaseChar(char character, int modifiers) {

        int keyCode = keyCodes.getOrDefault(
                Character.toUpperCase(character),
                -1
        );
        if(keyCode == -1) return;
        releaseKey(keyCode);
    }
    public static void typeChar(char character, int modifiers) {
        var keyboardAccessor = VisorAPI.client().getGuiManager()
                .getOverlayManager()
                .getKeyboardAccessor();
        Screen screen = keyboardAccessor.getAttachedTo();
        if(screen != null){
            //overlays
            screen.charTyped(character,modifiers);
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen != null) {
            Minecraft.getInstance().keyboardHandler.charTyped(
                    Minecraft.getInstance().getWindow().getWindow(),
                    character, modifiers
            );

        }
        //keybindings
        int keyCode = keyCodes.getOrDefault(
                Character.toUpperCase(character),
                -1
        );
        if(keyCode == -1) return;
        pressKey(keyCode);
        releaseKey(keyCode);

    }
    public static void typeChar(char character) {
        typeChar(character, 0);
    }
    public static void typeChars(CharSequence characters) {
        int i = characters.length();

        for (int j = 0; j < i; ++j) {
            char character = characters.charAt(j);
            typeChar(character);
        }
    }



    static {
            keyCodes.put('`', GLFW.GLFW_KEY_GRAVE_ACCENT);
            keyCodes.put('1', GLFW.GLFW_KEY_1);
            keyCodes.put('2', GLFW.GLFW_KEY_2);
            keyCodes.put('3', GLFW.GLFW_KEY_3);
            keyCodes.put('4', GLFW.GLFW_KEY_4);
            keyCodes.put('5', GLFW.GLFW_KEY_5);
            keyCodes.put('6', GLFW.GLFW_KEY_6);
            keyCodes.put('7', GLFW.GLFW_KEY_7);
            keyCodes.put('8', GLFW.GLFW_KEY_8);
            keyCodes.put('9', GLFW.GLFW_KEY_9);
            keyCodes.put('0', GLFW.GLFW_KEY_0);
            keyCodes.put('-', GLFW.GLFW_KEY_MINUS);
            keyCodes.put('=', GLFW.GLFW_KEY_EQUAL);
            keyCodes.put('Q', GLFW.GLFW_KEY_Q);
            keyCodes.put('W', GLFW.GLFW_KEY_W);
            keyCodes.put('E', GLFW.GLFW_KEY_E);
            keyCodes.put('R', GLFW.GLFW_KEY_R);
            keyCodes.put('T', GLFW.GLFW_KEY_T);
            keyCodes.put('Y', GLFW.GLFW_KEY_Y);
            keyCodes.put('U', GLFW.GLFW_KEY_U);
            keyCodes.put('I', GLFW.GLFW_KEY_I);
            keyCodes.put('O', GLFW.GLFW_KEY_O);
            keyCodes.put('P', GLFW.GLFW_KEY_P);
            keyCodes.put('[', GLFW.GLFW_KEY_LEFT_BRACKET);
            keyCodes.put(']', GLFW.GLFW_KEY_RIGHT_BRACKET);
            keyCodes.put('\\', GLFW.GLFW_KEY_BACKSLASH);
            keyCodes.put('A', GLFW.GLFW_KEY_A);
            keyCodes.put('S', GLFW.GLFW_KEY_S);
            keyCodes.put('D', GLFW.GLFW_KEY_D);
            keyCodes.put('F', GLFW.GLFW_KEY_F);
            keyCodes.put('G', GLFW.GLFW_KEY_G);
            keyCodes.put('H', GLFW.GLFW_KEY_H);
            keyCodes.put('J', GLFW.GLFW_KEY_J);
            keyCodes.put('K', GLFW.GLFW_KEY_K);
            keyCodes.put('L', GLFW.GLFW_KEY_L);
            keyCodes.put(';', GLFW.GLFW_KEY_SEMICOLON);
            keyCodes.put('\'', GLFW.GLFW_KEY_APOSTROPHE);
            keyCodes.put('Z', GLFW.GLFW_KEY_Z);
            keyCodes.put('X', GLFW.GLFW_KEY_X);
            keyCodes.put('C', GLFW.GLFW_KEY_C);
            keyCodes.put('V', GLFW.GLFW_KEY_V);
            keyCodes.put('B', GLFW.GLFW_KEY_B);
            keyCodes.put('N', GLFW.GLFW_KEY_N);
            keyCodes.put('M', GLFW.GLFW_KEY_M);
            keyCodes.put(',', GLFW.GLFW_KEY_COMMA);
            keyCodes.put('.', GLFW.GLFW_KEY_PERIOD);
            keyCodes.put('/', GLFW.GLFW_KEY_SLASH);

    }

}
