package org.vmstudio.visor.core.client.gui.overlays.options;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.gui.screens.overlayoptions.OptionsScreenButtonTemplate;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Getter
public class OverlayOptionsButtonTemplate extends OverlayOptionGroup<OverlayOptionsButtonTemplate> {
    public static final String ID = "button_template";
    private static final Component NAME = Component.translatable("visor.overlay.options." + ID);

    private int width;
    private int height;
    private String text;

    private CustomizationType customizationType;

    //COLOR CUSTOMIZATION
    private AtumColor color;
    private AtumColor textColor;


    private boolean transparentBackground;

    //TEXTURE CUSTOMIZATION
    private String rawTexturePath;



    private GuiTexture texturePath;

    private boolean worldOnly;


    private String key;

    private int keyCode;

    public OverlayOptionsButtonTemplate(@NotNull VROverlay owner,
                                        @NotNull Consumer<OverlayOptionsButtonTemplate> defaultSettings) {
        super(owner, defaultSettings);
    }

    @Override
    public void update(boolean force) {

    }

    @Override
    protected void onLoad(@NotNull Config config) {
        width = config.getIntOrDefault("width", 60);
        height = config.getIntOrDefault("height", 60);
        text = config.getStringOrDefault("text", "E");
        setKey(config.getStringOrDefault("key", "e"));

        customizationType = CustomizationType.valueOf(
                config.getStringOrDefault(
                        "customizationType", CustomizationType.COLOR.name())
        );

        try {
            color = AtumColor.immutableFromString(config.getStringOrDefault("color", AtumColor.GRAY.asString()));
        }catch (Exception e){
            color = AtumColor.GRAY;
        }
        try {
            textColor = AtumColor.immutableFromString(config.getStringOrDefault("textColor", AtumColor.WHITE.asString()));
        }catch (Exception e){
            textColor = AtumColor.WHITE;
        }

       transparentBackground = config.getBool("transparentBackground")
                || color.getAlphaInt() == 0;
        color = opaque(color);
        textColor = opaque(textColor);
        worldOnly = config.getBool("world_only");

        var defaultTexture = VisorAddon.MISSING_ICON.getResourceLocation();
        rawTexturePath = config.getStringOrDefault(
                "texturePath",
                defaultTexture.getNamespace()
                        + ResourceLocation.NAMESPACE_SEPARATOR
                        + defaultTexture.getPath()
        );

        setTexturePath(rawTexturePath);

        changesNotSaved = true;
    }

    @Override
    protected void onSave(@NotNull Config config) {
        config.set("width", width);
        config.set("height", height);
        config.set("key", key);

        config.set("customizationType", customizationType.name());

        config.set("color", color.asString());
        config.set("textColor", textColor.asString());
        config.set("transparentBackground", transparentBackground);
        config.set("text", text);

        config.set("texturePath", rawTexturePath);

        config.set("world_only", worldOnly);

    }


    public void setWidth(int width) {
        this.width = Math.max(10, width);
        changesNotSaved = true;
    }

    public void setHeight(int height) {
        this.height = Math.max(10, height);
        changesNotSaved = true;
    }


    public void setTexturePath(@Nullable String rawValue) {
        this.rawTexturePath = rawValue;

        try {
            if (rawTexturePath == null || rawTexturePath.isBlank()) {
                this.rawTexturePath = null;
            }
            if (this.rawTexturePath != null) {
                if (!rawTexturePath.endsWith(".png")) {
                    this.rawTexturePath = null;
                } else {
                    ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
                    var resourceLoc = new ResourceLocation(rawTexturePath);
                    var resource = resourceManager.getResource(resourceLoc);
                    if (resource.isEmpty()) {
                        this.rawTexturePath = null;
                    }
                }
            }
        } catch (Exception e) {
            this.rawTexturePath = null;
        }

        this.texturePath = this.rawTexturePath != null
                ? GuiTexture.of(new ResourceLocation(this.rawTexturePath))
                : VisorAddon.MISSING_ICON;

        changesNotSaved = true;
    }


    @Nullable
    public GuiTexture getTexture(){
        return customizationType == CustomizationType.TEXTURE
                ? texturePath
                : null;
    }



    @Nullable
    public AtumColor getFillColor(){
        if (customizationType != CustomizationType.COLOR || transparentBackground) {
            return null;
        }
        return color;
    }

    private static AtumColor opaque(@NotNull AtumColor value) {
        if (value.getAlphaInt() == 255) {
            return value;
        }
        return AtumColor.immutable(
                value.getRedInt(), value.getGreenInt(), value.getBlueInt(), 255
        );
    }
    public void setText(@Nullable String buttonText) {
        this.text = buttonText == null ? "" : buttonText;
        changesNotSaved = true;
    }

    public void setKey(@Nullable String key) {
        this.key = key == null ? "" : key.trim();
        this.keyCode = InputHelper.getKeyCode(this.key);
        changesNotSaved = true;
    }


    public void setColor(AtumColor color) {
        if (color == null) {
            return;
        }
        this.transparentBackground = color.getAlphaInt() == 0;
        this.color = opaque(color);
        changesNotSaved = true;
    }
    public void setTextColor(AtumColor color) {
        if (color == null) {
            return;
        }
        this.textColor = opaque(color);
        changesNotSaved = true;
    }

    public void setTransparentBackground(boolean flag) {
        this.transparentBackground = flag;
        changesNotSaved = true;
    }
    public void setCustomizationType(CustomizationType type){
        this.customizationType = type;
        changesNotSaved = true;
    }

    public void setWorldOnly(boolean flag) {
        this.worldOnly = flag;
        changesNotSaved = true;
    }

    @Override
    public boolean supportsCopying() {
        return true;
    }

    @Override
    public @NotNull OptionsScreen<?> getScreen() {
        return new OptionsScreenButtonTemplate(this);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return NAME;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }


    public enum CustomizationType{
        COLOR,
        TEXTURE
    }
}