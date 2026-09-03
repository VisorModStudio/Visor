package org.vmstudio.visor.core.client.gui.overlays.builtin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayHelper;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import org.vmstudio.visor.api.client.player.pose.PoseAnchor;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import org.vmstudio.visor.core.client.utils.ClientUtils;

import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VROverlayFullscreenWarning extends VROverlayScreen {
    public static String ID = "fullscreen_warning";
    protected static ResourceLocation RESOURCE = McVersionUtils.newResourceLoc("visor:textures/gui/overlays/warning.png");
    protected OverlayOptionsPose optionsPose;

    public VROverlayFullscreenWarning(@NotNull VisorAddon owner, @NotNull String id) {
        super(owner, id, ComponentPriority.HIGHEST, 1.0f);
        optionsPose = getOption(OverlayOptionsPose.ID, OverlayOptionsPose.class);
        setEnabled(true);
    }

    @Override
    protected void init() {
        clearWidgets();
    }

    @Override
    protected void onPreRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int pulsePeriodMs = 1100;
        float phase = (Util.getMillis() % pulsePeriodMs) / (float) pulsePeriodMs;
        float pulse = (Mth.sin(phase * Mth.TWO_PI) + 1.0f) * 0.5f;

        int backgroundColor = 0xF0121417;
        int glowRgb = 0xFFC24A;
        int glowAlphaMax = 0x4D;

        guiGraphics.fill(0, 0, width, height, backgroundColor);
        guiGraphics.fill(0, 0, width, height, ((int) Mth.lerp(pulse, 0, glowAlphaMax) << 24) | glowRgb);

        int iconMargin = 4;
        int iconSize = height - iconMargin * 2;
        float iconAlphaMin = 0.45f;

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, Mth.lerp(pulse, iconAlphaMin, 1.0f));
        GuiTexture warningIcon = new GuiTexture(
                RESOURCE,
                0, 0,
                16, 16,
                16, 16
        );
        warningIcon.blit(
                guiGraphics,
                iconMargin, iconMargin,
                iconSize, iconSize
        );
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        int textColor = 0xFFF2E9D8;
        int descriptionColor = 0xFFB6AE9F;
        int lineGap = 2;
        int textX = iconMargin * 2 + iconSize;

        List<FormattedCharSequence> lines = descriptionLines();
        int blockHeight = font.lineHeight + lines.size() * (font.lineHeight + lineGap);
        int textY = (height - blockHeight) / 2;

        guiGraphics.drawString(font, warningText(), textX, textY, textColor, false);
        textY += font.lineHeight + lineGap;

        for (FormattedCharSequence line : lines) {
            guiGraphics.drawString(font, line, textX, textY, descriptionColor, false);
            textY += font.lineHeight + lineGap;
        }
    }

    private Component warningText() {
        return Component.translatable("visor.overlay." + ID + ".title");
    }

    private List<FormattedCharSequence> descriptionLines() {
        int maxTextWidth = 480;
        return MC.font.split(Component.translatable("visor.overlay." + ID + ".text"), maxTextWidth);
    }

    @Override
    protected void onUpdatePose(float partialTicks) {
        float referenceWidth = 480f;
        float scale = optionsPose.getScale() * (getRequestedWidthScaled() / referenceWidth);

        VROverlayHelper.applyPose(
                this,
                optionsPose.getPositionAnchor(),
                optionsPose.getRotationAnchor(),
                scale,
                optionsPose.isAimedRotation(),
                optionsPose.getPositionOffset(),
                optionsPose.getRotationOffset()
        );
    }

    @Override
    protected boolean updateVisibility() {
        return ClientUtils.isFullscreenInVr();
    }

    @Override
    public boolean supportsCursor() {
        return false;
    }

    @Override
    public boolean supportsLight() {
        return false;
    }

    @Override
    public boolean isInViewDistance() {
        return true;
    }

    @Override
    public int getRequestedWidth() {
        int scaleFactor = Math.max(1, guiScaleFactor);
        int textPadding = 8;
        int textWidth = MC.font.width(warningText());
        for (FormattedCharSequence line : descriptionLines()) {
            textWidth = Math.max(textWidth, MC.font.width(line));
        }

        return getRequestedHeight() + (textWidth + textPadding) * scaleFactor;
    }

    @Override
    public int getRequestedHeight() {
        int verticalPadding = 8;
        int lineGap = 2;
        int lineHeight = MC.font.lineHeight;
        int lines = 1 + descriptionLines().size();

        return (verticalPadding * 2
                + lineHeight * lines
                + lineGap * (lines - 1)) * Math.max(1, guiScaleFactor);
    }

    @Override
    public @NotNull Component getName() {
        return Component.translatable("visor.overlay.%s.name".formatted(getId()));
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.translatable("visor.overlay.%s.description".formatted(getId()));
    }

    @Override
    protected @NotNull List<OverlayOptionGroup<?>> createOptions() {
        return List.of(
                new OverlayOptionsPose(
                        this,
                        it -> {
                            it.setTickPose(true);
                            it.setAimedRotation(true);
                            it.setPositionAnchor(PoseAnchor.HMD);
                            it.setPositionOffset(
                                    0f,
                                    0f,
                                    -1.0f
                            );
                            it.setRotationAnchor(PoseAnchor.HMD);
                            it.setRotationOffset(
                                    0f,
                                    0f,
                                    0f
                            );
                            it.setScale(0.5f);
                        }
                )
        );
    }
}