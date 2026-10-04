package org.vmstudio.visor.core.client.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.client.VRPlayMode;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McGuiUtils;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McScreen;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.utils.ClientUtils;

import java.util.List;

public class VRInactiveScreen extends McScreen {
    private static final Identifier HEADSET_ICON = McVersionUtils.newResourceLoc("visor:icon_headset.png");
    private static final int ICON_TEXTURE_WIDTH = 256;
    private static final int ICON_TEXTURE_HEIGHT = 144;
    private static final int ICON_WIDTH = 64;
    private static final int ICON_HEIGHT = 36;

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 12;

    @Nullable
    private final Screen parent;

    private List<FormattedCharSequence> titleLines;
    private int iconY;
    private int titleY;

    public VRInactiveScreen(@Nullable Screen parent) {
        super(Component.translatable("visor.screen.vr_inactive.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        titleLines = font.split(title, width - 40);
        int titleHeight = titleLines.size() * font.lineHeight;
        iconY = (height - ICON_HEIGHT - GAP - titleHeight - GAP - BUTTON_HEIGHT) / 2;
        titleY = iconY + ICON_HEIGHT + GAP;

        addRenderableWidget(Button.builder(
                        Component.translatable("visor.screen.vr_inactive.button.exit_vr"),
                        b -> exitVR())
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .pos((width - BUTTON_WIDTH) / 2, titleY + titleHeight + GAP)
                .build()
        );
    }

    @Override
    protected void renderScreenBackground(@NotNull GuiGraphicsExtractor gfx, int mx, int my, float pt) {
        gfx.fill(0, 0, width, height, 0xFF000000);
    }

    @Override
    protected void renderContents(@NotNull GuiGraphicsExtractor gfx, int mx, int my, float pt) {
        McGuiUtils.blit(gfx, HEADSET_ICON,
                (width - ICON_WIDTH) / 2, iconY, ICON_WIDTH, ICON_HEIGHT,
                0, 0, ICON_TEXTURE_WIDTH, ICON_TEXTURE_HEIGHT,
                ICON_TEXTURE_WIDTH, ICON_TEXTURE_HEIGHT);

        int y = titleY;
        for (var line : titleLines) {
            McGuiUtils.drawString(gfx, font, line, (width - font.width(line)) / 2, y, 0xFFFFFFFF, false);
            y += font.lineHeight;
        }
        super.renderContents(gfx, mx, my, pt);
    }

    @Override
    public void tick() {
        if (VisorState.get().isNotActive()) {
            onClose();
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        McVersionClientUtils.setScreen(parent);
    }

    private void exitVR() {
        if (minecraft.level != null) {
            ClientUtils.disconnect("VR disabled");
        } else {
            onClose();
        }
        VisorState.setVrPlayMode(VRPlayMode.DISABLED);
        ClientContext.settingsManager.saveOptions();
    }
}
