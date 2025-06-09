package me.phoenixra.visor.core.client.gui.screens;

import me.phoenixra.visor.api.client.gui.widgets.TextScrollWidget;
import me.phoenixra.visor.core.client.exceptions.VRInitException;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class VRErrorScreen extends Screen {
    private final Screen previousScreen;
    private final Component error;

    public VRErrorScreen(Component title, Component error) {
        super(title);
        this.previousScreen = Minecraft.getInstance().screen;
        this.error = error;
    }

    @Override
    protected void init() {

        this.addRenderableWidget(
                new TextScrollWidget(
                        this.width / 2 - 155,
                        30, 310,
                        this.height - 30 - 36,
                        this.error
                )
        );

        this.addRenderableWidget(
                new Button.Builder(
                        Component.literal("Back"),
                        (p) -> Minecraft.getInstance()
                                .setScreen(this.previousScreen)
                ).pos(this.width / 2 + 55, this.height - 32)
                        .size(100, 20)
                        .build()
        );
        this.addRenderableWidget(
                new Button.Builder(
                        Component.literal("Report error"),
                        (p) -> Minecraft.getInstance().setScreen(new VRReportError())
                ).pos(this.width / 2 - 50, this.height - 32)
                        .size(100, 20)
                        .build()
        );
        this.addRenderableWidget(
                new Button.Builder(
                        Component.literal("Copy"),
                        (p) -> Minecraft.getInstance()
                                .keyboardHandler.setClipboard(
                                        this.title.getString() + "\n" + this.error.getString()
                                )
                ).pos(this.width / 2 - 155, this.height - 32)
                        .size(100, 20)
                        .build()
        );
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2, 15,
                0xFFFFFF
        );
    }


    public static void catchError(Throwable throwable, boolean logError){
        if(logError) {
            LoggerUtils.printError(throwable);
        }
        if(throwable instanceof  VRInitException initException){
            Minecraft.getInstance().setScreen(
                    new VRErrorScreen(initException.getTitle(), initException.getError())
            );
        }else{
            Minecraft.getInstance().setScreen(
                    new VRErrorScreen(
                            Component.literal("Error: "+throwable.getClass().getName()),
                            LoggerUtils.throwableToComponent(throwable)
                    )
            );
        }
    }
}
