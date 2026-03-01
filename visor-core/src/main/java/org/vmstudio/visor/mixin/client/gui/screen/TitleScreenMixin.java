package org.vmstudio.visor.mixin.client.gui.screen;

import org.vmstudio.visor.api.client.VRPlayMode;
import org.vmstudio.visor.api.client.VRStateMode;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.vmstudio.visor.core.client.ClientContext;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private Button visor$vrModeButton;
    @Unique
    private VRPlayMode visor$playModeLast;

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void visor$onTick(CallbackInfo ci){
        var currentPlayMode = VRClientSettings.getVrPlayMode();
        if(visor$playModeLast != currentPlayMode){
            Component text =  Component.translatable(
                    "visor.options.common.vr_play_mode",
                    Component.translatable(
                            "visor.options.enums.VRPlayMode."+
                                    currentPlayMode.name()
                    )
            );
            visor$vrModeButton.setMessage(
                    text
            );
            visor$playModeLast = currentPlayMode;
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", shift = At.Shift.AFTER, ordinal = 1), method = "createNormalMenuOptions")
    public void visor$initFullGame(CallbackInfo ci) {
        visor$addVRModeButton();
    }

    @Inject(at = @At("TAIL"), method = "createDemoMenuOptions")
    public void visor$initDemo(CallbackInfo ci) {
        visor$addVRModeButton();
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void visor$renderToolTip(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {

        if (visor$vrModeButton.visible && visor$vrModeButton.isMouseOver(i, j)) {
            guiGraphics.renderTooltip(
                    font,
                    font.split(
                            Component.translatable("visor.options.common.vr_play_mode.tooltip"),
                            Math.max(width / 2 - 43, 170)
                    ),
                    i, j
            );
        }
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

        Component text =  Component.translatable(
                "visor.options.common.vr_play_mode",
                Component.translatable(
                        "visor.options.enums.VRPlayMode."+
                                VRClientSettings.getVrPlayMode().name()
                )
        );
        visor$vrModeButton = new Button.Builder(
                text,
                (button) -> {
                    var playMode = VRClientSettings.getVrPlayMode();
                    VRClientSettings.setVrPlayMode(
                            playMode.next()
                    );
                    ClientContext.settingsManager.saveOptions();
                    button.setMessage(
                            text
                    );
                    visor$playModeLast = VRClientSettings.getVrPlayMode();
                })
                .size(76, 20)
                .pos(this.width / 2 + 104, this.height / 4 + 72)
                .build();
        visor$vrModeButton.visible = true;

        visor$playModeLast = VRClientSettings.getVrPlayMode();

        this.addRenderableWidget(visor$vrModeButton);
    }



}
