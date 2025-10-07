package me.phoenixra.visor.api.client.gui.overlay.options.types;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.Config;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionsScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Getter @Setter
public class OverlayOptionsGlobal extends OverlayOptionGroup<OverlayOptionsGlobal> {
    public static final String ID = "global";
    private static final Component NAME = Component.translatable("visor.overlay.options."+ID);

    private OptionsUpdaterType optionsUpdaterType;



    public OverlayOptionsGlobal(@NotNull VROverlay owner,
                                @NotNull Consumer<OverlayOptionsGlobal> defaultSettings){
        super(owner, defaultSettings);
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
    public @NotNull OverlayOptionsScreen<?> getScreen() {
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
