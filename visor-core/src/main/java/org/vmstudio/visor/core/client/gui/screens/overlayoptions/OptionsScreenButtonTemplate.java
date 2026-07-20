package org.vmstudio.visor.core.client.gui.screens.overlayoptions;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.SliderWidget;
import org.vmstudio.visor.api.client.gui.widgets.color.ColorPickerWidgetSet;
import org.vmstudio.visor.api.client.gui.widgets.color.ColorSampleButton;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSlider;
import org.vmstudio.visor.core.client.gui.overlays.options.OverlayOptionsButtonTemplate;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OptionsScreenButtonTemplate extends OptionsScreen<OverlayOptionsButtonTemplate> {

    private static final int FIELD_HEIGHT = 18;
    private static final int ROW_SPACING = 32;
    private static final int GAP = 6;


    private Page page = Page.MAIN;

    private ColorTarget colorTarget = ColorTarget.FILL;


    private EditBoxImaged widthField;
    private EditBoxImaged heightField;

    private EditBoxImaged buttonTextField;
    private ColorSampleButton textColorSample;

    private EditBoxImaged keyField;

    private ButtonImaged visibilityButton;

    private SliderWidget<OverlayOptionsButtonTemplate.CustomizationType> customizationTypeSlider;

    private ColorSampleButton colorSample;

    private EditBoxImaged textureField;

    private ColorPickerWidgetSet colorPicker;
    private ButtonImaged backButton;

    public OptionsScreenButtonTemplate(@NotNull OverlayOptionsButtonTemplate optionsGroup) {
        super(optionsGroup, Background.VERTICAL_WIDER);
    }

    @Override
    protected void onInit() {
        if (page == Page.COLOR) {
            initColorPage();
            return;
        }
        initMainPage();
    }

    private void initMainPage() {

        int startX = cursorBoundsX + 10;
        int fullW = cursorBoundsWidth - 20;
        int halfW = (fullW - GAP) / 2;
        int y = cursorBoundsY + 12 + 10;

        // Row 1
        widthField = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(startX, y)
                        .size(halfW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable("visor.overlay.options.button_template.width"))
                        .setFilter( s -> s.matches("\\d*"))
        );
        widthField.setValue(String.valueOf(optionsGroup.getWidth()));
        widthField.setResponder(text -> {
            try {
                optionsGroup.setWidth(Integer.parseInt(text));
            } catch (NumberFormatException ignored) {
            }
        });


        heightField =new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(startX + halfW + GAP, y)
                        .size(halfW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable("visor.overlay.options.button_template.height"))
                        .setFilter( s -> s.matches("\\d*"))
        );
        heightField.setValue(String.valueOf(optionsGroup.getHeight()));
        heightField.setResponder(text -> {
            try {
                optionsGroup.setHeight(Integer.parseInt(text));
            } catch (NumberFormatException ignored) {
            }
        });


        // Row 2
        y += ROW_SPACING;

        buttonTextField = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(startX, y)
                        .size(halfW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable("visor.overlay.options.button_template.text"))
        );
        buttonTextField.setValue(optionsGroup.getText() != null ? optionsGroup.getText() : "");
        buttonTextField.setResponder(optionsGroup::setText);
        buttonTextField.setMaxLength(64);


        textColorSample = new ColorSampleButton(
                startX + halfW + GAP, y,
                halfW, FIELD_HEIGHT,
                colorOf(ColorTarget.TEXT),
                it -> openColorPage(ColorTarget.TEXT)
        );


        // Row 3
        y += ROW_SPACING;

        int visibilityButtonW = 80;
        int visibilityButtonX = startX + (fullW - visibilityButtonW) / 2;

        visibilityButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(visibilityButtonX, y)
                        .size(visibilityButtonW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setText(visibilityText())
                        .highlight(
                                OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT
                        ),
                button -> {
                    optionsGroup.setWorldOnly(!optionsGroup.isWorldOnly());
                    button.setMessage(visibilityText());
                }
        );


        // Row 4
        y += ROW_SPACING;

        int keyFieldW = halfW;
        int keyFieldX = startX + (fullW - keyFieldW) / 2;

        keyField = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(keyFieldX, y)
                        .size(keyFieldW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable("visor.overlay.options.button_template.key"))
        );
        keyField.setValue(optionsGroup.getKey());
        keyField.setResponder(optionsGroup::setKey);
        keyField.setMaxLength(32);


        // Row 5
        y += ROW_SPACING;

        int modeW = 60;
        int modeX = startX + (fullW - modeW) / 2;


        customizationTypeSlider = new SliderWidget<>(
                new WidgetInfoSlider()
                        .pos(modeX, y)
                        .size(modeW, 20)
                        .setBackgroundTexture(OptionTextures.GRAY_TEXTURE)
                        .setKnobTexture(OptionTextures.LIGHT_GRAY_TEXTURE_2)
                        .setDynamicTextScale(true)
                        .setTextColor(AtumColor.WHITE),
                List.of(OverlayOptionsButtonTemplate.CustomizationType.values()),
                slider -> {
                    optionsGroup.setCustomizationType(slider.getSelected());
                    slider.setText(modeText());
                    init();
                });
        customizationTypeSlider.setSelected(
                optionsGroup.getCustomizationType(),
                false
        );
        customizationTypeSlider.setText(modeText());


        // Row 6
        y += ROW_SPACING;

        addRenderableWidget(widthField);
        addRenderableWidget(heightField);
        addRenderableWidget(buttonTextField);
        addRenderableWidget(textColorSample);
        addRenderableWidget(visibilityButton);
        addRenderableWidget(keyField);
        addRenderableWidget(customizationTypeSlider);

        if (optionsGroup.getCustomizationType()
                == OverlayOptionsButtonTemplate.CustomizationType.COLOR) {
            initColorFields(startX, y, fullW);
        } else {
            initTextureFields(startX, y, fullW);
        }

    }

    private void initColorFields(int baseX, int y, int fieldW) {

        colorSample = new ColorSampleButton(
                baseX, y,
                fieldW, FIELD_HEIGHT,
                colorOf(ColorTarget.FILL),
                it -> openColorPage(ColorTarget.FILL)
        );
        addRenderableWidget(colorSample);
    }


    private void initTextureFields(int baseX, int y, int fieldW) {


        textureField = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(baseX, y)
                        .size(fieldW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable("visor.overlay.options.button_template.texture"))
        );
        textureField.setValue(optionsGroup.getRawTexturePath() != null
                ? optionsGroup.getRawTexturePath()
                : ""
        );
        textureField.setResponder(optionsGroup::setTexturePath);
        textureField.setMaxLength(256);
        addRenderableWidget(textureField);
    }


    private void initColorPage() {

        int startX = cursorBoundsX + 10;
        int fullW = cursorBoundsWidth - 20;
        int pickerY = cursorBoundsY + 12 + 14;

        colorPicker = new ColorPickerWidgetSet(
                startX, pickerY, fullW,
                colorOf(colorTarget),
                // only the background can be turned off - transparent text
                // would just be invisible
                colorTarget == ColorTarget.FILL,
                this::applyTargetColor
        );
        colorPicker.initWidgets().forEach(this::addRenderableWidget);

        backButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(startX, pickerY + colorPicker.getHeight() + GAP + 2)
                        .size(fullW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setText(Component.translatable("visor.button.back"))
                        .highlight(
                                OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT
                        ),
                button -> {
                    page = Page.MAIN;
                    init();
                }
        );
        addRenderableWidget(backButton);
    }

    @Override
    public void tick() {
        super.tick();
        if (page == Page.COLOR && colorPicker != null) {
            colorPicker.onTick();
        }
    }

    @Override
    protected void onRender(GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTick) {
        if (page == Page.COLOR) {
            renderColorPage(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }
        renderMainPage(guiGraphics);
    }

    private void openColorPage(@NotNull ColorTarget target) {
        this.colorTarget = target;
        this.page = Page.COLOR;
        init();
    }

    private AtumColor colorOf(@NotNull ColorTarget target) {
        if (target == ColorTarget.TEXT) {
            AtumColor textColor = optionsGroup.getTextColor();
            return textColor != null ? textColor : AtumColor.WHITE;
        }

        AtumColor color = optionsGroup.getColor();
        if (color == null) {
            color = AtumColor.WHITE;
        }
        if (!optionsGroup.isTransparentBackground()) {
            return color;
        }
        return AtumColor.immutable(
                color.getRedInt(), color.getGreenInt(), color.getBlueInt(), 0
        );
    }

    private void applyTargetColor(@NotNull AtumColor color) {
        if (colorTarget == ColorTarget.FILL) {
            optionsGroup.setColor(color);
            return;
        }
        optionsGroup.setTextColor(color);
    }


    private void renderMainPage(GuiGraphics guiGraphics) {

        int startX = cursorBoundsX + 10;
        int fullW = cursorBoundsWidth - 20;
        int halfW = (fullW - GAP) / 2;
        int labelY = cursorBoundsY + 3 + 10;

        // ---- Row 1 ----
        guiGraphics.drawString(font, Component.translatable(
                        "visor.overlay.options.button_template.width"),
                startX, labelY,
                0xFFFFFF
        );
        guiGraphics.drawString(font, Component.translatable(
                "visor.overlay.options.button_template.height"), startX + halfW + GAP, labelY, 0xFFFFFF);

        // ---- Row 2 ----
        labelY += ROW_SPACING;
        guiGraphics.drawString(font, Component.translatable(
                "visor.overlay.options.button_template.text"), startX, labelY, 0xFFFFFF);
        guiGraphics.drawString(font, Component.translatable(
                "visor.overlay.options.button_template.text_color"), startX + halfW + GAP, labelY, 0xFFFFFF);

        // ---- Row 3  ----
        labelY += ROW_SPACING;

        // ---- Row 4 ----
        labelY += ROW_SPACING;
        Component keyLabel = Component.translatable("visor.overlay.options.button_template.key");
        int keyLabelW = font.width(keyLabel);
        guiGraphics.drawString(font, keyLabel,
                startX + (fullW - keyLabelW) / 2, labelY, 0xFFFFFF);

        // ---- Row 6 ----
        labelY += ROW_SPACING * 2;
        Component customizationLabel = optionsGroup.getCustomizationType()
                == OverlayOptionsButtonTemplate.CustomizationType.COLOR
                ? Component.translatable("visor.overlay.options.button_template.color")
                : Component.translatable("visor.overlay.options.button_template.texture");
        guiGraphics.drawString(font, customizationLabel, startX, labelY, 0xFFFFFF);
    }

    private void renderColorPage(GuiGraphics guiGraphics,
                                 int mouseX, int mouseY,
                                 float partialTick) {

        int startX = cursorBoundsX + 10;
        int titleY = cursorBoundsY + 12 + 3;

        guiGraphics.drawString(font, colorTarget.title(), startX, titleY, 0xFFFFFF);

        if (colorPicker != null) {
            colorPicker.onPreRender(guiGraphics, mouseX, mouseY, partialTick);
        }
    }


    private Component visibilityText() {
        return Component.translatable(
                "visor.overlay.options.button_template.visible",
                Component.translatable(optionsGroup.isWorldOnly()
                        ? "visor.overlay.options.button_template.visible.world"
                        : "visor.overlay.options.button_template.visible.always")
        );
    }

    private Component modeText() {
        return Component.translatable(
                "visor.overlay.options.button_template.mode",
                optionsGroup.getCustomizationType().name()
        );
    }


    private enum Page {
        MAIN,
        COLOR
    }


    private enum ColorTarget {
        FILL("visor.overlay.options.button_template.color"),
        TEXT("visor.overlay.options.button_template.text_color");

        private final String titleKey;

        ColorTarget(String titleKey) {
            this.titleKey = titleKey;
        }

        Component title() {
            return Component.translatable(titleKey);
        }
    }
}
