package me.phoenixra.visor.core.client.gui.overlays.options;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.helpers.TexturesHelper;
import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlays.options.OptionsScreen;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.OptionsScreenKeyButton;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Getter
public class OverlayOptionsKeyButton extends OverlayOptionGroup<OverlayOptionsKeyButton> {
    public static final String ID = "key_button";
    private static final Component NAME = Component.translatable("visor.overlay.options." + ID);

    private int width;
    private int height;
    private String text;

    private CustomizationType customizationType;

    //COLOR CUSTOMIZATION
    private AtumColor color;
    private AtumColor textColor;

    //TEXTURE CUSTOMIZATION
    private String rawTexturePath;



    private GuiTexture textureColor;
    private GuiTexture texturePath;

    private boolean worldOnly;

    private char key;

    public OverlayOptionsKeyButton(@NotNull VROverlay owner,
                                   @NotNull Consumer<OverlayOptionsKeyButton> defaultSettings) {
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
        key = config.getStringOrDefault("key", "e").charAt(0);

        customizationType = CustomizationType.valueOf(
                config.getStringOrDefault(
                        "customizationType", CustomizationType.COLOR.name())
        );

        color = parseColor(config.getStringOrDefault("color", colorToString(AtumColor.GRAY)));

        textColor = parseColor(config.getStringOrDefault("textColor", colorToString(AtumColor.WHITE)));

        worldOnly = config.getBool("world_only");

        var defaultTexture = VisorAddon.MISSING_ICON.getResourceLocation();
        rawTexturePath = config.getStringOrDefault(
                "texturePath",
                defaultTexture.getNamespace()
                        + ResourceLocation.NAMESPACE_SEPARATOR
                        + defaultTexture.getPath()
        );

        textureColor = TexturesHelper.getColorGuiTexture(color);
        setTexturePath(rawTexturePath);

        changesNotSaved = true;
    }

    @Override
    protected void onSave(@NotNull Config config) {
        config.set("width", width);
        config.set("height", height);
        config.set("key", key);

        config.set("customizationType", customizationType.name());

        config.set("color", colorToString(color));
        config.set("textColor", colorToString(textColor));
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
    private static String colorToString(@Nullable AtumColor color) {
        if (color == null) return "128;128;128";
        return color.getRedInt() + ";" + color.getGreenInt() + ";" + color.getBlueInt();
    }

    private static @Nullable AtumColor parseColor(@Nullable String text) {
        if (text == null || text.isBlank()) return null;
        String[] parts = text.split(";");
        if (parts.length != 3) return null;
        try {
            int r = clampComponent(Integer.parseInt(parts[0].trim()));
            int g = clampComponent(Integer.parseInt(parts[1].trim()));
            int b = clampComponent(Integer.parseInt(parts[2].trim()));
            return AtumColor.immutable(r, g, b, 255);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private static int clampComponent(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public GuiTexture getTexture(){
        return customizationType == CustomizationType.TEXTURE
                ? texturePath
                : textureColor;
    }
    public void setText(@Nullable String buttonText) {
        this.text = buttonText == null ? "" : buttonText;
        changesNotSaved = true;
    }

    public void setKey(char key) {
        this.key = key;
        changesNotSaved = true;
    }

    public void setColor(AtumColor color) {
        this.color = color;
        this.textureColor = TexturesHelper.getColorGuiTexture(color);
        changesNotSaved = true;
    }
    public void setTextColor(AtumColor color) {
        this.textColor = color;
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
        return new OptionsScreenKeyButton(this);
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