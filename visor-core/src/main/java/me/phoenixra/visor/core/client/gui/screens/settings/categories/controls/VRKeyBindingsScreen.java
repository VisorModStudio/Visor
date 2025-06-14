package me.phoenixra.visor.core.client.gui.screens.settings.categories.controls;

import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VRKeyBindingsScreen extends Screen {
    private final Screen previousScreen;
    protected VRKeyBindingsScreen(Screen oldScreen) {
        super(Component.translatable("visor.option.screen.keyBindings"));
        this.previousScreen = oldScreen;
    }

    //"Load defaults",
    // "Apply changes"
    // List of actions and their keybinds
    @Override
    protected void init() {
        this.addRenderableWidget(
                new Button.Builder(
                        Component.translatable("gui.back"),
                        (button) -> {
                            ClientContext.settingsHandler.saveOptions();
                            this.minecraft.setScreen(this.previousScreen);
                        }
                ).pos(this.width / 2 + 5, this.height - 30)
                        .size(150, 20)
                        .build()
        );
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == 256) {
            ClientContext.settingsHandler.saveOptions();
            this.minecraft.setScreen(this.previousScreen);

            return true;
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }
}
