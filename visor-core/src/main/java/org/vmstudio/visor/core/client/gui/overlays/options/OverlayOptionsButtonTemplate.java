package org.vmstudio.visor.core.client.gui.overlays.options;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.client.input.action.VRAction;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import org.vmstudio.visor.core.client.gui.screens.overlayoptions.OptionsScreenButtonTemplate;
import org.vmstudio.visor.core.client.input.actions.ActionLeftMouse;
import org.vmstudio.visor.core.client.input.actions.ActionMiddleMouse;
import org.vmstudio.visor.core.client.input.actions.ActionRightMouse;
import org.vmstudio.visor.core.client.input.actions.ActionScrollMouse;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    private String rawHoverTexturePath;
    private GuiTexture hoverTexturePath;

    private boolean worldOnly;


    private String key;

    private int keyCode;

    // ---- Actions on press ----
    private ActionType actionType = ActionType.KEY;

    private String command = "";

    private final Map<String, VisibilityAction> overlayActions = new LinkedHashMap<>();

    private String vrActionSetId = "";
    private String vrActionId = "";

    private AtumColor hoverColor;

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
        setHoverTexturePath(config.getStringOrDefault("hoverTexturePath", ""));

        actionType = parseEnum(
                config.getStringOrDefault("actionType", ActionType.KEY.name()),
                ActionType.class,
                ActionType.KEY
        );
        command = config.getStringOrDefault("command", "");

        overlayActions.clear();
        String rawOverlayActions = config.getStringOrDefault("overlayVisibility", "");
        for (String token : rawOverlayActions.split(";")) {
            if (token.isBlank()) continue;
            int sep = token.indexOf(':');
            if (sep <= 0) continue;
            String overlayId = token.substring(0, sep).trim().toLowerCase(Locale.ROOT);
            VisibilityAction action = parseEnum(
                    token.substring(sep + 1), VisibilityAction.class, null
            );
            if (overlayId.isEmpty() || action == null) continue;
            overlayActions.put(overlayId, action);
        }

        vrActionSetId = config.getStringOrDefault("vrActionSet", "").trim().toLowerCase(Locale.ROOT);
        vrActionId = config.getStringOrDefault("vrAction", "").trim().toLowerCase(Locale.ROOT);

        hoverColor = null;
        String rawHoverColor = config.getStringOrDefault("hoverColor", "");
        if (!rawHoverColor.isBlank()) {
            try {
                AtumColor parsed = AtumColor.immutableFromString(rawHoverColor);
                if (parsed.getAlphaInt() != 0) {
                    hoverColor = opaque(parsed);
                }
            } catch (Exception ignored) {
            }
        }

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

        config.set("actionType", actionType.name());
        config.set("command", command);
        config.set("overlayVisibility", serializeOverlayActions());
        config.set("vrActionSet", vrActionSetId);
        config.set("vrAction", vrActionId);
        config.set("hoverColor", hoverColor == null ? "" : hoverColor.asString());
        config.set("hoverTexturePath", rawHoverTexturePath == null ? "" : rawHoverTexturePath);

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
                    var resourceLoc = McVersionUtils.newResourceLoc(rawTexturePath);
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
                ? GuiTexture.of(McVersionUtils.newResourceLoc(this.rawTexturePath))
                : VisorAddon.MISSING_ICON;

        changesNotSaved = true;
    }


    public void setHoverTexturePath(@Nullable String rawValue) {
        this.rawHoverTexturePath = rawValue;

        try {
            if (rawHoverTexturePath == null || rawHoverTexturePath.isBlank()) {
                this.rawHoverTexturePath = null;
            }
            if (this.rawHoverTexturePath != null) {
                if (!rawHoverTexturePath.endsWith(".png")) {
                    this.rawHoverTexturePath = null;
                } else {
                    ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
                    var resourceLoc = McVersionUtils.newResourceLoc(rawHoverTexturePath);
                    var resource = resourceManager.getResource(resourceLoc);
                    if (resource.isEmpty()) {
                        this.rawHoverTexturePath = null;
                    }
                }
            }
        } catch (Exception e) {
            this.rawHoverTexturePath = null;
        }

        this.hoverTexturePath = this.rawHoverTexturePath != null
                ? GuiTexture.of(McVersionUtils.newResourceLoc(this.rawHoverTexturePath))
                : null;

        changesNotSaved = true;
    }


    @Nullable
    public GuiTexture getTexture(){
        return customizationType == CustomizationType.TEXTURE
                ? texturePath
                : null;
    }


    @Nullable
    public GuiTexture getHoverTexture(){
        return customizationType == CustomizationType.TEXTURE
                ? hoverTexturePath
                : null;
    }



    @Nullable
    public AtumColor getFillColor(){
        if (customizationType != CustomizationType.COLOR || transparentBackground) {
            return null;
        }
        return color;
    }


    @Nullable
    public AtumColor getHoverFillColor(){
        if (customizationType != CustomizationType.COLOR || hoverColor == null) {
            return null;
        }
        return hoverColor;
    }

    private static AtumColor opaque(@NotNull AtumColor value) {
        if (value.getAlphaInt() == 255) {
            return value;
        }
        return AtumColor.immutable(
                value.getRedInt(), value.getGreenInt(), value.getBlueInt(), 255
        );
    }

    private String serializeOverlayActions() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, VisibilityAction> e : overlayActions.entrySet()) {
            if (sb.length() > 0) sb.append(';');
            sb.append(e.getKey()).append(':').append(e.getValue().name());
        }
        return sb.toString();
    }

    private static <E extends Enum<E>> E parseEnum(@Nullable String value,
                                                   @NotNull Class<E> type,
                                                   @Nullable E fallback) {
        if (value == null) return fallback;
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return fallback;
        }
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

    public void setActionType(@NotNull ActionType type) {
        this.actionType = type;
        changesNotSaved = true;
    }

    public void setCommand(@Nullable String command) {
        this.command = command == null ? "" : command;
        changesNotSaved = true;
    }


    public void setHoverColor(@Nullable AtumColor color) {
        if (color == null || color.getAlphaInt() == 0) {
            this.hoverColor = null;
        } else {
            this.hoverColor = opaque(color);
        }
        changesNotSaved = true;
    }


    public void setOverlayAction(@NotNull String overlayId,
                                 @Nullable VisibilityAction action) {
        String id = overlayId.trim().toLowerCase(Locale.ROOT);
        if (id.isEmpty()) return;
        if (action == null) {
            overlayActions.remove(id);
        } else {
            overlayActions.put(id, action);
        }
        changesNotSaved = true;
    }

    @Nullable
    public VisibilityAction getOverlayAction(@NotNull String overlayId) {
        return overlayActions.get(overlayId.trim().toLowerCase(Locale.ROOT));
    }

    public void clearOverlayActions() {
        overlayActions.clear();
        changesNotSaved = true;
    }

    public void setVrAction(@Nullable String actionSetId, @Nullable String actionId) {
        this.vrActionSetId = actionSetId == null ? "" : actionSetId.trim().toLowerCase(Locale.ROOT);
        this.vrActionId = actionId == null ? "" : actionId.trim().toLowerCase(Locale.ROOT);
        changesNotSaved = true;
    }

    public static boolean isMouseAction(@NotNull VRAction action) {
        return action instanceof ActionLeftMouse
                || action instanceof ActionRightMouse
                || action instanceof ActionMiddleMouse
                || action instanceof ActionScrollMouse;
    }

    public static List<VRActionButton> getSelectableActions(@NotNull VRActionSet actionSet) {
        return actionSet.getActions().stream()
                .filter(VRActionButton.class::isInstance)
                .map(VRActionButton.class::cast)
                .filter(it -> !isMouseAction(it))
                .toList();
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


    public enum ActionType{
        KEY,
        COMMAND,
        OVERLAY_VISIBILITY,
        VR_ACTION
    }


    public enum VisibilityAction{
        TOGGLE,
        SHOW,
        HIDE
    }
}