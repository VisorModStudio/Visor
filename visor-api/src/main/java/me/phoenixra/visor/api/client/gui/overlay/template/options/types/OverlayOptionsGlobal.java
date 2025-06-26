package me.phoenixra.visor.api.client.gui.overlay.template.options.types;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.Config;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptionsBase;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptionsScreen;
import me.phoenixra.visor.api.client.gui.overlay.template.OverlayTemplate;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

//@TODO maybe should not call update in onTick and onRender always? make optional?
@Getter @Setter
public class OverlayOptionsGlobal extends OverlayOptionsBase<OverlayOptionsGlobal> {
    public static final String ID = "global";

    private UpdateOptionsType updateOptionsType;

    private float overlayScale;


    @Nullable
    private String formulaOverlayScale;
    private boolean updatableOverlayScale;



    public OverlayOptionsGlobal(@NotNull OverlayTemplate owner,
                                @NotNull Consumer<OverlayOptionsGlobal> defaultSettings){
        super(owner, defaultSettings);
    }


    @Override
    public void update(boolean force) {
        var configManager = owner.getConfig().getConfigOwner();
        if(force){
            overlayScale = 1.0f;
            try{
                if(formulaOverlayScale != null) {
                    overlayScale = (float) Double.parseDouble(formulaOverlayScale);
                }
                updatableOverlayScale = false;
            }catch (NumberFormatException e){

                overlayScale = (float) VRMathUtils.getEvaluated(configManager, formulaOverlayScale);
                updatableOverlayScale = true;
            }
            overlayScale = overlayScale <= 0 ? 1.0f : overlayScale;
            return;
        }
        if(updatableOverlayScale) {
            float overlayScale = (float) VRMathUtils.getEvaluated(configManager, formulaOverlayScale);
            overlayScale = overlayScale <= 0 ? 1.0f : overlayScale;
            this.overlayScale = overlayScale;
        }
    }

    @Override
    protected void onLoad(@NotNull Config section){
        var configManager = owner.getConfig().getConfigOwner();

        updateOptionsType = UpdateOptionsType.valueOf(
                section.getStringOrDefault("update_options", UpdateOptionsType.OFF.name())
        );

        formulaOverlayScale = section.getStringOrNull("overlayScale");
        overlayScale = 1.0f;
        try{
            if(formulaOverlayScale != null) {
                overlayScale = (float) Double.parseDouble(formulaOverlayScale);
            }
            updatableOverlayScale = false;
        }catch (NumberFormatException e){

            overlayScale = (float) VRMathUtils.getEvaluated(configManager, formulaOverlayScale);
            updatableOverlayScale = true;
        }
        overlayScale = overlayScale <= 0 ? 1.0f : overlayScale;

    }
    @Override
    public void onSave(@NotNull Config section){
        section.set("update_options", updateOptionsType.name());
        section.set("overlayScale", formulaOverlayScale);
    }

    @Override
    public @NotNull OverlayOptionsScreen<?> getScreen(float mainMenuWidth, float mainMenuHeight) {
        return VisorAPI.client().getGuiManager().getOverlayManager().getOptionsScreenFor(
                this,mainMenuWidth,mainMenuHeight
        );
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("visor.overlaySettings.global");

    }

    @Override
    public @NotNull String getId() {
        return ID;
    }

    public enum UpdateOptionsType{
        OFF(Component.translatable("options.off")),
        TICK(Component.translatable("visor.button.tick")),
        FRAME(Component.translatable("visor.button.frame"));

        private final Component displayName;
        UpdateOptionsType(Component displayName){
            this.displayName = displayName;
        }
        public Component getName(){
            return displayName;
        }
        public UpdateOptionsType next(){
            switch (this){
                case OFF -> {
                    return TICK;
                }
                case TICK -> {
                    return FRAME;
                }
                case FRAME -> {
                    return OFF;
                }
            }
            return OFF;
        }
    }
}
