package org.vmstudio.visor.api.client.gui.overlays.options.types;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Getter
public class OverlayOptionsMisc extends OverlayOptionGroup<OverlayOptionsMisc> {
    public static final String ID = "misc";
    private static final Component NAME = Component.translatable("visor.overlay.options."+ID);

    private OptionsUpdaterType optionsUpdaterType;



    public OverlayOptionsMisc(@NotNull VROverlay owner,
                              @NotNull Consumer<OverlayOptionsMisc> defaultSettings){
        super(owner, defaultSettings);
    }

    public void setOptionsUpdaterType(OptionsUpdaterType newValue) {
        if(this.optionsUpdaterType == newValue){
            return;
        }
        this.optionsUpdaterType = newValue;
        changesNotSaved = true;
    }

    @Override
    public void update(boolean force) {
    }

    @Override
    protected void onLoad(@NotNull Config config){

        optionsUpdaterType = OptionsUpdaterType.valueOf(
                config.getStringOrDefault("update_options", OptionsUpdaterType.OFF.name())
        );


    }
    @Override
    public void onSave(@NotNull Config config){
        config.set("update_options", optionsUpdaterType.name());
    }

    @Override
    public boolean supportsCopying() {
        return true;
    }

    @Override
    public @NotNull OptionsScreen<?> getScreen() {
        return VisorAPI.client().getGuiManager().getOverlayManager().getOptionsScreenFor(
                this
        );
    }

    @Override
    public @NotNull Component getDisplayName() {
        return NAME;

    }

    @Override
    public @NotNull String getId() {
        return ID;
    }

    public enum OptionsUpdaterType {
        OFF(Component.translatable("options.off")),
        TICK(Component.translatable("visor.button.tick")),
        FRAME(Component.translatable("visor.button.frame"));

        private final Component displayName;
        OptionsUpdaterType(Component displayName){
            this.displayName = displayName;
        }
        public Component getName(){
            return displayName;
        }
        public OptionsUpdaterType next(){
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
