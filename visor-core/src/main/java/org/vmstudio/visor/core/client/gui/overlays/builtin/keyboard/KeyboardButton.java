package org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard;

import org.vmstudio.visor.api.compatibility.mcversion.gui.McPlainButton;
import org.vmstudio.visor.core.client.gui.screens.VRKeyboardScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
//? if >=1.21.9 {
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;

public class KeyboardButton extends McPlainButton {
    private VRKeyboardScreen keyboardScreen;
    private final OnRelease onRelease;
    private boolean pressed;

    private boolean usePressTask = true;

    private boolean hoveredSecondary;
    protected KeyboardButton(VRKeyboardScreen keyboardScreen,
                             int x, int y, int width, int height,
                             Component component,
                             OnPress onPress,
                             OnRelease onRelease,
                             CreateNarration createNarration
    ) {
        super(
                x, y,
                width, height,
                component,
                onPress,
                createNarration
        );
        this.keyboardScreen = keyboardScreen;
        this.onRelease = onRelease;
    }

    // before super.renderContents: the button sprite reads isHovered()
    @Override
    protected void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        VROverlayKeyboard overlayKeyboard = keyboardScreen.getOverlayKeyboard();
        if(overlayKeyboard.getInactiveCursorData().isInGui()){
            int mX = overlayKeyboard.getInactiveCursorData().getCursorX();
            int mY = overlayKeyboard.getInactiveCursorData().getCursorY();
            hoveredSecondary = mX >= this.getX()
                    && mY >= this.getY()
                    && mX < this.getX() + this.width
                    && mY < this.getY() + this.height;
        }else{
            hoveredSecondary = false;
        }

        super.renderContents(guiGraphics, i, j, f);
    }

    //? if >=1.21.9 {
    @Override
    public void onPress(InputWithModifiers modifiers) {
        if(usePressTask) {
            keyboardScreen.setPressedTask(() -> super.onPress(modifiers));
            keyboardScreen.setPressTick(0);
        }
        super.onPress(modifiers);
        pressed = true;
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        if(onRelease != null && pressed) {
            onRelease.onRelease(this);
        }
        pressed = false;
    }
    //?} else {
    /*@Override
    public void onPress() {
        if(usePressTask) {
            keyboardScreen.setPressedTask(super::onPress);
            keyboardScreen.setPressTick(0);
        }
        super.onPress();
        pressed = true;
    }

    @Override
    public void onRelease(double d, double e) {
        if(onRelease != null && pressed) {
            onRelease.onRelease(this);
        }
        pressed = false;
    }
    *///?}

    @Override
    public boolean isHovered() {
        return super.isHovered() || hoveredSecondary;
    }

    public static class Builder {
        private VRKeyboardScreen keyboardScreen;
        private final Component message;
        private final OnPress onPress;
        private OnRelease onRelease;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private boolean usePressTask = true;

        public Builder(VRKeyboardScreen keyboardScreen, Component component, OnPress onPress) {
            this.keyboardScreen = keyboardScreen;
            this.message = component;
            this.onPress = onPress;
        }

        public Builder usePressTask(boolean flag) {
            this.usePressTask = flag;
            return this;
        }
        public Builder onRelease(OnRelease onRelease) {
            this.onRelease = onRelease;
            return this;
        }
        public Builder pos(int i, int j) {
            this.x = i;
            this.y = j;
            return this;
        }

        public Builder width(int i) {
            this.width = i;
            return this;
        }

        public Builder size(int i, int j) {
            this.width = i;
            this.height = j;
            return this;
        }


        public KeyboardButton build() {
            KeyboardButton button = new KeyboardButton(
                    keyboardScreen,
                    this.x, this.y,
                    this.width, this.height,
                    this.message,
                    this.onPress,
                    this.onRelease,
                    Button.DEFAULT_NARRATION
            );
            button.usePressTask = this.usePressTask;
            return button;
        }
    }


    public interface OnRelease {
        void onRelease(Button button);
    }
}
