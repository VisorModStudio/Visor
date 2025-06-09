package me.phoenixra.visor.core.client.gui.screens;

import me.phoenixra.visor.api.client.gui.widgets.TextScrollWidget;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class VRReportError extends Screen {
    private final Screen previousScreen;
    private final String discord;

    public VRReportError() {
        super(Component.translatable("visor.messages.report_error"));
        this.previousScreen = Minecraft.getInstance().screen;
        discord = Component.translatable("visor.messages.discord_link").getString();
    }

    @Override
    protected void init() {

        this.addRenderableWidget(
                new TextScrollWidget(
                        this.width / 2 - 155,
                        30, 310,
                        this.height - 30 - 36,
                        Component.translatable("visor.messages.report_error_message")
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
                        Component.literal("Our Discord"),
                        (p) -> Util.getPlatform().openUri(discord)
                ).pos(this.width / 2 - 50, this.height - 32)
                        .size(100, 20)
                        .build()
        );
        this.addRenderableWidget(
                new Button.Builder(
                        Component.literal("Mc Folder"),
                        (p) -> Util.getPlatform().openUri(Minecraft.getInstance().gameDirectory.toURI())
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
}
