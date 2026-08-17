package org.vmstudio.visor.core.client.gui.screens;

import org.vmstudio.visor.core.client.exceptions.VisorException;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VRErrorReportScreen extends Screen {
    private final String discordUrl;
    private final String logsFolderUrl;

    private final Component summary;
    private List<FormattedCharSequence> summaryLines;

    public VRErrorReportScreen(Component title, Throwable t) {
        super(title);
        this.discordUrl = Component.translatable("visor.messages.discord_link").getString();
        this.logsFolderUrl = Minecraft.getInstance()
                .gameDirectory
                .toPath()
                .resolve("logs")
                .toUri()
                .toString();

        this.summary = Component.translatable("visor.messages.error.summary");

    }

    @Override
    protected void init() {
        int maxWidth = this.width - 40;
        this.summaryLines = this.font.split(this.summary, maxWidth);

        final int btnW = 100;
        final int btnH = 20;
        final int gap = 10;
        final int rowCnt = 3;

        int bottomY = this.height - 32;

        int totalW = btnW * rowCnt + gap * (rowCnt - 1);
        int startX = (this.width - totalW) / 2;

        // Back
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.back"),
                        b -> Minecraft.getInstance().setScreen(new TitleScreen()))
                .size(btnW, btnH)
                .pos(startX, bottomY)
                .build()
        );

        // Open Logs folder
        addRenderableWidget(Button.builder(
                        Component.translatable("visor.button.open_logs"),
                        b -> Util.getPlatform().openUri(logsFolderUrl))
                .size(btnW, btnH)
                .pos(startX + (btnW + gap), bottomY)
                .build()
        );

        // Discord
        addRenderableWidget(Button.builder(
                        Component.translatable("visor.button.discord"),
                        b -> Util.getPlatform().openUri(discordUrl))
                .size(btnW, btnH)
                .pos(startX + (btnW + gap) * 2, bottomY)
                .build()
        );
    }

    @Override
    public void render(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(gfx, mouseX, mouseY, partialTick);

        gfx.drawCenteredString(this.font, this.title, this.width/2, 15, 0xFF5555);

        int y = 40;
        for (var line : summaryLines) {
            int lineWidth = this.font.width(line);
            int x = (this.width - lineWidth) / 2;
            gfx.drawString(this.font, line, x, y, 0xFFFFFF, false);
            y += this.font.lineHeight;
        }

        super.render(gfx, mouseX, mouseY, partialTick);
    }

    public static void catchError(Throwable t, boolean log) {
        if (log) LoggerUtils.printError(t);

        Component title = (t instanceof VisorException vx)
                ? vx.getTitle()
                : Component.translatable("visor.messages.error.generic");

        Minecraft.getInstance().execute(() ->
                Minecraft.getInstance().setScreen(new VRErrorReportScreen(title, t))
        );
    }
}
