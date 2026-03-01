package org.vmstudio.visor.core.client.gui.screens.settings;

import lombok.Getter;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.helpers.ScaleHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRSettingsScreen extends Screen {

    public static AtumColor INACTIVE_COLOR = AtumColor.immutable(91,91,91,255);
    private static final ResourceLocation RESOURCE = new ResourceLocation(
            "visor:textures/gui/settings/general.png"
    );
    private static final int RESOURCE_WIDTH = 274;
    private static final int RESOURCE_HEIGHT = 260;

    public static final GuiTexture BACKGROUND = new GuiTexture(
            new ResourceLocation(
                    "visor:textures/gui/settings/background.png"
            ),
            0, 0,
            256, 159,
            256, 159
    );

    public static final GuiTexture PRESETS_BUTTON = new GuiTexture(
            RESOURCE,
            0, 108,
            208, 51,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );
    public static final GuiTexture PRESETS_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            0, 159,
            208, 50,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );
    public static final GuiTexture PRESETS_BUTTON_SELECTED = new GuiTexture(
            RESOURCE,
            0, 210,
            208, 50,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );

    public static final GuiTexture LOAD_DEFAULTS = new GuiTexture(
            RESOURCE,
            0, 52,
            50, 50,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );
    public static final GuiTexture LOAD_DEFAULTS_HOVERED = new GuiTexture(
            RESOURCE,
            52, 52,
            50, 50,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );
    public static final GuiTexture LOAD_DEFAULTS_INACTIVE = new GuiTexture(
            RESOURCE,
            104, 52,
            50, 50,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );
    public static final GuiTexture BACK = new GuiTexture(
            RESOURCE,
            104, 0,
            50, 50,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );
    public static final GuiTexture BACK_HOVERED = new GuiTexture(
            RESOURCE,
            156, 0,
            50, 50,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );
    public static final GuiTexture BACK_INACTIVE = new GuiTexture(
            RESOURCE,
            208, 0,
            50, 50,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );


    public static final GuiTexture KEY_MODIFIER_ON = new GuiTexture(
            RESOURCE,
            0, 0,
            50, 49,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );

    public static final GuiTexture KEY_MODIFIER_OFF = new GuiTexture(
            RESOURCE,
            52, 0,
            50, 49,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );

    public static final GuiTexture REMOVE_KEY_ACTION = new GuiTexture(
            RESOURCE,
            156, 52,
            50, 55,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );

    public static final GuiTexture CLOSE = new GuiTexture(
            RESOURCE,
            208, 52,
            25, 25,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );

    public static final GuiTexture SWITCH_BUTTON_LEFT = new GuiTexture(
            RESOURCE,
            235, 65,
            25, 12,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );
    public static final GuiTexture SWITCH_BUTTON_RIGHT = new GuiTexture(
            RESOURCE,
            235, 52,
            25, 12,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );

    public static final GuiTexture ADD_BUTTON = new GuiTexture(
            RESOURCE,
            208, 80,
            52, 25,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );

    public static final GuiTexture CHECKBOX_OFF = new GuiTexture(
            RESOURCE,
            262, 52,
            12, 12,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );
    public static final GuiTexture CHECKBOX_ON = new GuiTexture(
            RESOURCE,
            262, 65,
            12, 12,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );

    private final Screen previousScreen;


    //
    private ButtonImaged buttonClose;
    private ButtonImaged buttonBack;
    private ButtonImaged buttonLoadDefaults;

    //
    private ButtonImaged buttonOverlays;
    private ButtonImaged buttonAddons;
    private ButtonImaged buttonJoinCommunity;

    //
    private VRSettingsCategory settingsCategory = VRSettingsCategory.PRESETS;
    private VROptionsSet options;

    private List<ButtonImaged> settingsButtons;


    @Getter
    private final ScaleHelper scaleHelper = new ScaleHelper(
            BACKGROUND.getWidth(),
            BACKGROUND.getHeight()
    );

    private int startX;
    private int startY;

    @Getter
    private int optionsStartX, optionsStartY, optionsWidth, optionsHeight;

    private boolean initialized;
    public VRSettingsScreen(Screen previousScreen) {
        super(Component.empty());
        this.previousScreen = previousScreen;
    }


    @Override
    protected void init() {

        scaleHelper.computeScale(width, height);

        String discordUrl = Component.translatable("visor.messages.discord_link").getString();

        startX = scaleHelper.getStartX();
        startY = scaleHelper.getStartY();
        optionsStartX = scaleHelper.scaledX(56);
        optionsStartY = scaleHelper.scaledY(27);
        optionsWidth = scaleHelper.scaledSize(144);
        optionsHeight = scaleHelper.scaledSize(124);

        buttonClose = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(240),scaleHelper.scaledY(4))
                        .size(scaleHelper.scaledSize(10), scaleHelper.scaledSize(10))
                        .setTexture(CLOSE)
                        .setTooltip(Tooltip.create(Component.translatable("visor.button.close")))
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT),
                (it)->{
                    ClientContext.settingsManager.saveOptions();
                    MC.setScreen(this.previousScreen);
                }
        );
        buttonBack = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(38),scaleHelper.scaledY(135))
                        .size(scaleHelper.scaledSize(16), scaleHelper.scaledSize(16))
                        .textures(
                                BACK,
                                BACK_HOVERED,
                                BACK_HOVERED
                        )
                        .setTextureInactive(BACK_INACTIVE)
                        .setTooltip(Tooltip.create(Component.translatable("gui.back"))),
                (it)->{
                    options.previousOptions();
                }
        );

        buttonLoadDefaults = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(202),scaleHelper.scaledY(135))
                        .size(scaleHelper.scaledSize(16), scaleHelper.scaledSize(16))
                        .textures(
                                LOAD_DEFAULTS,
                                LOAD_DEFAULTS_HOVERED,
                                LOAD_DEFAULTS_HOVERED
                        ).setTextureInactive(LOAD_DEFAULTS_INACTIVE)
                        .setTooltip(Tooltip.create(Component.translatable("visor.button.load_defaults"))),
                (it)->{
                    options.loadDefaults();
                }
        );

        buttonOverlays = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(202), scaleHelper.scaledY(27))
                        .size(scaleHelper.scaledSize(50), scaleHelper.scaledSize(12))
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .setDynamicTextScale(true)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                        .setText(Component.translatable("visor.options.main.overlay_settings")),
                (it)->{
                    var overlay = ClientContext.overlayManager
                            .getOverlay(VROverlaySettings.ID);
                    assert overlay != null;
                    overlay.setEnabled(true);
                }
        );
        if(VisorState.get().isNotInitialized()){
            buttonOverlays.active = false;
            buttonOverlays.getWidgetInfo().setTextColor(INACTIVE_COLOR);
        }
        buttonAddons = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(202), scaleHelper.scaledY(41))
                        .size(scaleHelper.scaledSize(50), scaleHelper.scaledSize(12))
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .setDynamicTextScale(true)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                        .setText(Component.translatable("visor.options.main.addons")),
                (it)->{
                    MC.setScreen(new VRSettingsAddonsScreen(this));
                }
        );
        buttonJoinCommunity = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(202), scaleHelper.scaledY(55))
                        .size(scaleHelper.scaledSize(50), scaleHelper.scaledSize(12))
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .setDynamicTextScale(true)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                        .setText(Component.translatable("visor.options.main.join_community")),
                (it)->{
                    Util.getPlatform().openUri(discordUrl);
                }
        );

        settingsButtons = new ArrayList<>();

        int yOffset = 0;
        for(var category : VRSettingsCategory.values()){
            ButtonImaged button;
            if(category == VRSettingsCategory.PRESETS){
                button = new ButtonImaged(
                        new WidgetInfoButtonImaged()
                                .pos(scaleHelper.scaledX(4), scaleHelper.scaledY(27 + yOffset))
                                .size(scaleHelper.scaledSize(50), scaleHelper.scaledSize(12))
                                .setTexture(PRESETS_BUTTON)
                                .setTextureHovered(PRESETS_BUTTON_HOVERED)
                                .setTextureSelected(PRESETS_BUTTON_SELECTED)
                                .setInactiveOnSelected(false)
                                .setDynamicTextScale(true)
                                .setText(Component.translatable("visor.options.presets.button")),
                        (it)->{
                            for(var b : settingsButtons){
                                b.setSelected(false);
                            }
                            openCategory(category);
                            it.setSelected(true);

                        }
                );
                yOffset += 18;
            }else {
                button = new ButtonImaged(
                        new WidgetInfoButtonImaged()
                                .pos(scaleHelper.scaledX(4), scaleHelper.scaledY(27 + yOffset))
                                .size(scaleHelper.scaledSize(50), scaleHelper.scaledSize(12))
                                .setTexture(OptionTextures.BLACK_TEXTURE)
                                .setInactiveOnSelected(false)
                                .setDynamicTextScale(true)
                                .setHighlightEnabled(true)
                                .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                                .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                                .setText(Component.translatable("visor.options." + category.getCategory().getKey() + ".button")),
                        (it) -> {
                            for (var b : settingsButtons) {
                                b.setSelected(false);
                            }
                            openCategory(category);
                            it.setSelected(true);

                        }
                );
                yOffset += 14;
            }
            if(category == settingsCategory){
                button.setSelected(true);
            }

            settingsButtons.add(button);
        }


        options = initialized
                ? options
                : settingsCategory.getSupplier().apply(this);
        options.initWidgets();

        buttonBack.active = options.canOpenPreviousPage();

        initialized = true;
        repopulateWidgets();
    }

    @Override
    public void tick() {
        super.tick();
        options.onTick();
        buttonBack.active = options.canOpenPreviousPage();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(VisorState.get().isNotActive()) {
            guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        }
        BACKGROUND.blit(
                guiGraphics,
                startX,startY,
                scaleHelper.scaledSize(BACKGROUND.getWidth()),
                scaleHelper.scaledSize(BACKGROUND.getHeight())
        );
        VisorAPI.NOD_ICON.blit(
                guiGraphics,
                scaleHelper.scaledX(4),
                scaleHelper.scaledY(3),
                scaleHelper.scaledSize(12),
                scaleHelper.scaledSize(12)
        );
        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                Component.translatable("visor.options.main").getString(),
                AtumColor.WHITE.asInt(),
                scaleHelper.scaledX(56), scaleHelper.scaledY(3),
                scaleHelper.scaledSize(141), scaleHelper.scaledSize(12),
                true
        );
        options.onPreRender(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        options.onPostRender(guiGraphics, mouseX, mouseY, partialTick);

    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {

    }


    public void openCategory(@NotNull VRSettingsCategory category){
        this.settingsCategory = category;
        this.options = settingsCategory.getSupplier().apply(this);
        this.options.initWidgets();
        buttonBack.active = options.canOpenPreviousPage();
        buttonLoadDefaults.active = options.canLoadDefaults();
        repopulateWidgets();

    }
    public void switchOptions(@NotNull VROptionsSet options){
        this.options = options;
        this.options.initWidgets();
        buttonBack.active = options.canOpenPreviousPage();
        buttonLoadDefaults.active = options.canLoadDefaults();
        repopulateWidgets();
    }

    public void repopulateWidgets() {
        clearWidgets();
        addRenderableWidget(buttonClose);
        addRenderableWidget(buttonBack);
        addRenderableWidget(buttonLoadDefaults);
        addRenderableWidget(buttonOverlays);
        addRenderableWidget(buttonAddons);
        addRenderableWidget(buttonJoinCommunity);
        settingsButtons.forEach(this::addRenderableWidget);
        options.getWidgets().forEach(this::addRenderableWidget);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean success = super.mouseClicked(mouseX, mouseY, button);
        options.mouseClicked(mouseX, mouseY, button, success);
        return success;
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        options.mouseScrolled(mouseX, mouseY, delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
