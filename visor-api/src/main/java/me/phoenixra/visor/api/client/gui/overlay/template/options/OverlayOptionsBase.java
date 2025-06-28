package me.phoenixra.visor.api.client.gui.overlay.template.options;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.visor.api.client.gui.overlay.template.VROverlayTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class OverlayOptionsBase<T extends OverlayOptionsBase<T>> implements OverlayOptions {

    @Getter
    protected final VROverlayTemplate owner;

    protected final Consumer<T> defaultSettings;

    private final ConfigFile overlayConfig;

    public OverlayOptionsBase(@NotNull VROverlayTemplate owner,
                              @NotNull Consumer<T> defaultSettings) {
        this.owner = owner;
        this.defaultSettings = defaultSettings;
        this.overlayConfig = owner.getConfig();
        if (overlayConfig.getSubsectionOrNull(getId().toUpperCase()) == null) {
            saveDefaults();
        } else {
            load();
        }
    }


    protected abstract void onLoad(@NotNull Config section);

    protected abstract void onSave(@NotNull Config section);


    @Override
    public final void load() {
        onLoad(
                overlayConfig.getSubsection(
                        getId().toUpperCase()
                )
        );
    }

    @Override
    public final void save() {
        try {
            Config section = overlayConfig.getSubsection(
                    getId().toUpperCase()
            );
            onSave(
                    section
            );
            //to make sure subsection is attached to config
            // (helps in case there is no subsection at all)
            overlayConfig.set(getId().toUpperCase(), section);



            overlayConfig.save();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveDefaults() {
        defaultSettings.accept((T) this);
        save();
        load();
    }
}
