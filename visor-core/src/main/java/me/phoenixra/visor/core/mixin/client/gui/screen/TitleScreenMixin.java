package me.phoenixra.visor.core.mixin.client.gui.screen;

import me.phoenixra.visor.api.client.VRStateMode;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
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

import me.phoenixra.visor.core.client.ClientContext;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private Button visor$vrModeButton;
    @Unique
    private Button visor$updateButton;

    protected TitleScreenMixin(Component component) {
        super(component);
    }


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", shift = At.Shift.AFTER, ordinal = 1), method = "createNormalMenuOptions")
    public void visor$initFullGame(CallbackInfo ci) {
        visor$addVRModeButton();
    }

    @Inject(at = @At("TAIL"), method = "createDemoMenuOptions")
    public void visor$initDemo(CallbackInfo ci) {
        visor$addVRModeButton();
    }

    @Unique
    private void visor$addVRModeButton() {

        visor$vrModeButton = new Button.Builder(
                Component.translatable(
                        "visor.button.playMode",
                                Component.translatable(
                                        "visor.enums.playMode."+
                                        VRClientSettings.getVrPlayMode().name()
                                )
                ),
                (button) -> {
                    VRClientSettings.setVrPlayMode(
                            VRClientSettings.getVrPlayMode().next()
                    );
                    ClientContext.settingsHandler.saveOptions();
                    button.setMessage(
                            Component.translatable(
                            "visor.button.playMode",
                                    Component.translatable(
                                            "visor.enums.playMode."+
                                                    VRClientSettings.getVrPlayMode().name()
                                    )
                            )
                    );
                })
                .size(76, 20)
                .pos(this.width / 2 + 104, this.height / 4 + 72)
                .build();
        visor$vrModeButton.visible = true;

        this.addRenderableWidget(visor$vrModeButton);
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void visor$renderToolTip(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {

        if (visor$vrModeButton.visible && visor$vrModeButton.isMouseOver(i, j)) {
            guiGraphics.renderTooltip(
                    font,
                    font.split(
                            Component.translatable("visor.option.VR_PLAY_MODE.tooltip"),
                            Math.max(width / 2 - 43, 170)
                    ),
                    i, j
            );
        }
        if (VisorState.getState() == VRStateMode.INITIALIZED
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
        return VisorState.getState().isActive()
                ? 0.0F
                : alpha;
    }
}
