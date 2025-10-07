package me.phoenixra.visor.api.client.gui.overlay.options;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Getter
public abstract class OverlayOptionGroup<T extends OverlayOptionGroup<T>> {

    protected final VROverlay owner;

    protected final Consumer<T> defaultsSupplier;


    protected ConfigFile overlayConfig;

    protected boolean initialized;

    protected boolean changesNotSaved;

    public OverlayOptionGroup(@NotNull VROverlay owner,
                              @NotNull Consumer<T> defaultsSupplier) {
        this.owner = owner;
        this.defaultsSupplier = defaultsSupplier;
    }

    /**
     * Initialize options
     */
    public void init(){
        this.overlayConfig = owner.getOptionsConfig();
        if (overlayConfig.getSubsectionOrNull(getId().toUpperCase()) == null) {
            loadDefaults();
            save();
        } else {
            loadFromFile(false);
        }
        onInit();
        initialized = true;
    }

    /**
     * Load options from config file
     */
    public final void loadFromFile(boolean reloadFile) {
        if(reloadFile){
            try{
                overlayConfig.reload();
            }catch (Throwable e){
                throw new RuntimeException(e);
            }
        }
        onLoad(getConfigSection());
        changesNotSaved = false;
    }

    /**
     * Load options from specified config
     *
     * @param config the config
     */
    public final void loadFromConfig(@NotNull Config config) {
        onLoad(config);
        changesNotSaved = true;
    }

    /**
     * Load options from other instance
     *
     * @param other the other instance
     */
    public final void loadFromOther(@NotNull OverlayOptionGroup<?> other) {
        if(!supportsCopying()){
            throw new RuntimeException("This option group does not support direct copying");
        }
        if(!canCopyFrom(other)){
            throw new IllegalArgumentException(
                    "This option group cannot copy " +
                            "from the specified option group"
            );
        }
        onLoad(other.getConfigSection());
        changesNotSaved = true;
    }

    /**
     * Load option group defaults of the overlay
     */
    public void loadDefaults() {
        defaultsSupplier.accept((T) this);
        update(true);
        changesNotSaved = true;
    }

    /**
     * Save option group data to file
     */
    public final void save() {
        try {
            Config section = overlayConfig.getSubsection(
                    getId().toUpperCase()
            );
            onSave(section);
            //to make sure subsection is there
            overlayConfig.set(getId().toUpperCase(), section);

            overlayConfig.save();
            changesNotSaved = false;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Get config section of this option group
     *
     * @return the config section
     */
    public final Config getConfigSection(){
        return overlayConfig.getSubsection(
                getId().toUpperCase()
        );
    }

    /**
     * On option group initialize
     */
    protected void onInit(){
    }

    /**
     * Update option group data
     *
     * @param reset if reset all data
     */
    public void update(boolean reset){
    }

    /**
     * On save to config file.
     * <p>
     *     Should apply options data from <code>config</code>
     * </p>
     *
     * @param config the config section
     */
    protected abstract void onLoad(@NotNull Config config);

    /**
     * On save to config file.
     * <p>
     *     Should set options data to <code>config</code>
     * </p>
     *
     * @param config the config section
     */
    protected abstract void onSave(@NotNull Config config);

    /**
     * If supports copying data from other instance.
     *
     * @return true/false
     */
    public abstract boolean supportsCopying();

    /**
     * If data can be copied from other instance.
     *
     * @param other the other instance to copy from
     * @return true/false
     */
    public boolean canCopyFrom(@NotNull OverlayOptionGroup<?> other){
        if (!getClass().isAssignableFrom(other.getClass())) {
            return false;
        }
        return other.supportsCopying() && supportsCopying();
    }

    /**
     * Get screen associated with this option group
     *
     * @return the screen
     */
    @NotNull
    public abstract OverlayOptionsScreen<?> getScreen();


    /**
     * Get option group name
     *
     * @return the name component
     */
    @NotNull
    public abstract Component getDisplayName();

    /**
     * Get option group id
     *
     * @return the id
     */
    @NotNull
    public abstract String getId();



}
