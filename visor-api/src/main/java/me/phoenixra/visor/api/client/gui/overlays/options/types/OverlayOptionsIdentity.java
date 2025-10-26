package me.phoenixra.visor.api.client.gui.overlays.options.types;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlays.options.OptionsScreen;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Getter
public class OverlayOptionsIdentity extends OverlayOptionGroup<OverlayOptionsIdentity> {
    public static final String ID = "identity";
    private static final Component NAME = Component.translatable("visor.overlay.options."+ID);

    private Component name;

    private Component description;

    private GuiTexture icon;


    private String rawName;

    private String rawDescription;

    private String rawIcon;

    public OverlayOptionsIdentity(@NotNull VROverlay owner,
                                  @NotNull Consumer<OverlayOptionsIdentity> defaultSettings) {
        super(owner, defaultSettings);
    }

    @Override
    public void update(boolean force) {

    }

    @Override
    protected void onLoad(@NotNull Config config) {
        rawName = config.getStringOrNull("name");
        rawDescription = config.getStringOrNull("description");
        rawIcon = config.getStringOrNull("icon");

        setName(rawName);
        setDescription(rawDescription);
        setIcon(rawIcon);

        onChanged();
    }

    @Override
    protected void onSave(@NotNull Config config) {
        config.set("name", rawName);
        config.set("description", rawDescription);
        config.set("icon", rawIcon);
    }


    /**
     * Set overlay name
     *
     * @param rawValue the value, that is valid in config file
     */
    public void setName(@Nullable String rawValue) {
        String oldValue = rawName;
        this.rawName = rawValue;
        this.name = rawValue != null
                ? Component.translatable(rawValue)
                : Component.literal(owner.getId());
        if((oldValue == null && rawValue != null)
                || (oldValue != null && !oldValue.equals(rawValue))){
            onChanged();
        }

    }

    /**
     * Set overlay name
     *
     * @param rawValue the value, that is valid in config file
     */
    public void setDescription(@Nullable String rawValue) {
        String oldValue = rawDescription;
        this.rawDescription = rawValue;
        this.description = rawValue != null
                ? Component.translatable(rawValue)
                : Component.literal("No description");
        if((oldValue == null && rawValue != null)
                || (oldValue != null && !oldValue.equals(rawValue))){
            onChanged();
        }
    }

    /**
     * Set overlay name
     *
     * @param rawValue the value, that is valid in config file
     */
    public void setIcon(@Nullable String rawValue) {
        String oldValue = rawIcon;

        this.rawIcon = rawValue;

        try {
            if (rawIcon == null || rawIcon.isBlank()) {
                this.rawIcon = null;
            }
            if (this.rawIcon != null) {
                if (!rawIcon.endsWith(".png")) {
                    this.rawIcon = null;
                } else {
                    ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

                    var resourceLoc = new ResourceLocation(rawIcon);

                    var resource = resourceManager.getResource(resourceLoc);
                    if (resource.isEmpty()) {
                        this.rawIcon = null;
                    }
                }
            }

        } catch (Exception e) {
            this.rawIcon = null;
        }
        this.icon = this.rawIcon != null
                ? new GuiTexture(new ResourceLocation(this.rawIcon))
                : VisorAddon.MISSING_ICON;

        if((oldValue == null && rawValue != null)
                || (oldValue != null && !oldValue.equals(rawValue))){
            onChanged();
        }
    }


    public void onChanged(){
        if(owner instanceof VROverlayTemplate template){
            template.updateIdentity();
        }
        changesNotSaved = true;
    }

    @Override
    public void loadDefaults() {
        //disable load defaults
        if(!isInitialized()) {
            super.loadDefaults();
        }
    }

    @Override
    public boolean supportsCopying() {
        return false;
    }

    @Override
    public @NotNull OptionsScreen<?> getScreen() {
        return VisorAPI.client().getGuiManager()
                .getOverlayManager()
                .getOptionsScreenFor(
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

}
