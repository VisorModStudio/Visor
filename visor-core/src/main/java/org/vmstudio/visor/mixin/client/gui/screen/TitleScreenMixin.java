package org.vmstudio.visor.mixin.client.gui.screen;

import org.vmstudio.visor.api.client.VRPlayMode;
import org.vmstudio.visor.api.client.VRStateMode;
import org.vmstudio.visor.api.client.gui.widgets.lists.DropDownListWidget;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.vmstudio.visor.core.client.ClientContext;

import java.util.Arrays;
import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private DropDownListWidget visor$vrModeButton;
    @Unique
    private VRPlayMode visor$playModeLast;

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void visor$onTick(CallbackInfo ci) {
        if (visor$vrModeButton == null) return;
        var currentPlayMode = VRClientSettings.getVrPlayMode();
        if (visor$playModeLast != currentPlayMode) {
            visor$vrModeButton.setSelectedIndex(currentPlayMode.ordinal(), false);
            visor$playModeLast = currentPlayMode;
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void visor$renderVrInitFailedWarning(GuiGraphics gfx, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!VisorState.isVrInitFailed()) {
            return;
        }

        Component msg = Component.translatable("visor.messages.vr_init_failed");
        int padX = 6;
        int padY = 2;
        int boxW = font.width(msg) + padX * 2;
        int boxH = font.lineHeight + padY * 2;
        int x = (this.width - boxW) / 2;
        int y = 2;

        gfx.fill(x - 1, y - 1, x + boxW + 1, y + boxH + 1, 0xFF5DD9FF);
        gfx.fill(x, y, x + boxW, y + boxH, 0xE6050B14);
        gfx.drawCenteredString(font, msg, this.width / 2, y + padY, 0xFFFFFFFF);
    }

    @Inject(method = "init", at = @At("TAIL"), order = 9999)
    public void visor$initAddVRModeButton(CallbackInfo ci) {
        visor$addVRModeButton();
    }


    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void visor$dropdownClickPriority(double mouseX, double mouseY, int button,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (visor$vrModeButton != null
                && visor$vrModeButton.isExpanded()
                && visor$vrModeButton.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void visor$renderToolTip(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        if (VisorState.get() == VRStateMode.INITIALIZED
                && VRClientSettings.getVrPlayMode().canPlayVR()) {
            Component text = Component.translatable("visor.messages.vr_auto_switch");
            guiGraphics.renderTooltip(
                    font,
                    font.split(text, 280),
                    width / 2 - 140 - 12,
                    17
            );
        }
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PanoramaRenderer;render(FF)V"), method = "render", index = 1)
    public float visor$noPanorama(float alpha) {
        return VisorState.get().isActive()
                ? 0.0F
                : alpha;
    }

    @Unique
    private void visor$addVRModeButton() {
        VRPlayMode[] modes = VRPlayMode.values();

        List<Component> items = Arrays.stream(modes)
                .map(mode -> (Component) Component.translatable(
                        "visor.options.common.vr_play_mode",
                        Component.translatable("visor.options.enums.VRPlayMode." + mode.name())
                ))
                .toList();

        VRPlayMode currentMode = VRClientSettings.getVrPlayMode();

        visor$vrModeButton = DropDownListWidget.builder(items)
                .pos(this.width / 2 + 104, this.height / 4 + 72)
                .size(76, 20)
                .setVisibleItems(modes.length)
                .setStartIndex(currentMode.ordinal())
                .setMessage(Component.translatable("visor.options.common.vr_play_mode.tooltip"))
                .setResponder(index -> {
                    VRPlayMode mode = modes[index];
                    VisorState.setVrPlayMode(mode);
                    ClientContext.settingsManager.saveOptions();
                    visor$playModeLast = mode;
                })
                .build();

        visor$playModeLast = currentMode;
        this.addRenderableWidget(visor$vrModeButton);
    }
}