package org.vmstudio.visor.core.client.gui.screens.overlayoptions;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.SliderWidget;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSlider;
import org.vmstudio.visor.core.client.gui.overlays.options.OverlayOptionsKeyButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OptionsScreenKeyButton extends OptionsScreen<OverlayOptionsKeyButton> {

    private static final int FIELD_HEIGHT = 18;
    private static final int ROW_SPACING = 32;
    private static final int GAP = 6;


    private EditBoxImaged widthField;
    private EditBoxImaged heightField;

    private EditBoxImaged buttonTextField;
    private EditBoxImaged textColorField;

    private EditBoxImaged keyField;

    private ButtonImaged visibilityButton;

    private SliderWidget<OverlayOptionsKeyButton.CustomizationType> customizationTypeSlider;

    private EditBoxImaged colorField;

    private EditBoxImaged textureField;

    public OptionsScreenKeyButton(@NotNull OverlayOptionsKeyButton optionsGroup) {
        super(optionsGroup, Background.VERTICAL_WIDER);
    }

    @Override
    protected void onInit() {

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
                        .setHint(Component.translatable("visor.overlay.options.key_button.width"))
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
                        .setHint(Component.translatable("visor.overlay.options.key_button.height"))
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
                        .setHint(Component.translatable("visor.overlay.options.key_button.text"))
        );
        buttonTextField.setValue(optionsGroup.getText() != null ? optionsGroup.getText() : "");
        buttonTextField.setResponder(optionsGroup::setText);
        buttonTextField.setMaxLength(64);


        textColorField =new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(startX + halfW + GAP, y)
                        .size(halfW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable("visor.overlay.options.key_button.text_color"))
        );
        textColorField.setValue(colorToString(optionsGroup.getTextColor()));
        textColorField.setResponder(text -> {
            AtumColor parsed = parseColor(text);
            if (parsed != null) {
                optionsGroup.setTextColor(parsed);
            }
        });


        // Row 3
        y += ROW_SPACING;

        int visibilityButtonW = 80;
        int visibilityButtonX = startX + (fullW - visibilityButtonW) / 2;

        visibilityButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(visibilityButtonX, y)
                        .size(visibilityButtonW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setText(Component.translatable("visor.overlay.options.key_button.visible",
                                Component.translatable(optionsGroup.isWorldOnly() ? "visor.overlay.options.key_button.visible.world" : "visor.overlay.options.key_button.visible.always")))
                        .highlight(
                                OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT
                        ),
                button -> {
                    optionsGroup.setWorldOnly(!optionsGroup.isWorldOnly());
                    button.setMessage(Component.translatable("Visible: "+(optionsGroup.isWorldOnly()?"In-game":"Always")));
                }
        );


        // Row 4
        y += ROW_SPACING;

        int keyFieldW = 40;
        int keyFieldX = startX + (fullW - keyFieldW) / 2;

        keyField = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(keyFieldX, y)
                        .size(keyFieldW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable("visor.overlay.options.key_button.key"))
        );
        keyField.setValue(String.valueOf(optionsGroup.getKey()));
        keyField.setResponder(text -> {
            if (!text.isEmpty()) {
                optionsGroup.setKey(text.charAt(0));
            }
        });
        keyField.setMaxLength(1);


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
                List.of(OverlayOptionsKeyButton.CustomizationType.values()),
                slider -> {
                    optionsGroup.setCustomizationType(slider.getSelected());
                    slider.setText(Component.translatable("visor.overlay.options.key_button.mode", optionsGroup.getCustomizationType().name()));
                    init();
                });
        customizationTypeSlider.setSelected(
                optionsGroup.getCustomizationType(),
                false
        );
        customizationTypeSlider.setText(Component.literal("Mode: " + optionsGroup.getCustomizationType().name()));


        // Row 6
        y += ROW_SPACING;

        addRenderableWidget(widthField);
        addRenderableWidget(heightField);
        addRenderableWidget(buttonTextField);
        addRenderableWidget(textColorField);
        addRenderableWidget(visibilityButton);
        addRenderableWidget(keyField);
        addRenderableWidget(customizationTypeSlider);

        if (optionsGroup.getCustomizationType()
                == OverlayOptionsKeyButton.CustomizationType.COLOR) {
            initColorFields(startX, y, fullW);
        } else {
            initTextureFields(startX, y, fullW);
        }

    }

    private void initColorFields(int baseX, int y, int fieldW) {

        colorField = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(baseX, y)
                        .size(fieldW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable("visor.overlay.options.key_button.key"))
        );
        colorField.setValue(colorToString(optionsGroup.getColor()));
        colorField.setResponder(text -> {
            AtumColor parsed = parseColor(text);
            if (parsed != null) {
                optionsGroup.setColor(parsed);
            }
        });
        colorField.setMaxLength(32);
        addRenderableWidget(colorField);
    }


    private void initTextureFields(int baseX, int y, int fieldW) {


        textureField = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(baseX, y)
                        .size(fieldW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable("visor.overlay.options.key_button.texture"))
        );
        textureField.setValue(optionsGroup.getRawTexturePath() != null
                ? optionsGroup.getRawTexturePath()
                : ""
        );
        textureField.setResponder(optionsGroup::setTexturePath);
        textureField.setMaxLength(256);
        addRenderableWidget(textureField);
    }


    @Override
    protected void onRender(GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTick) {

        int startX = cursorBoundsX + 10;
        int fullW = cursorBoundsWidth - 20;
        int halfW = (fullW - GAP) / 2;
        int labelY = cursorBoundsY + 3 + 10;

        // ---- Row 1 ----
        guiGraphics.drawString(font, Component.translatable(
                        "visor.overlay.options.key_button.width"),
                startX, labelY,
                0xFFFFFF
        );
        guiGraphics.drawString(font, Component.translatable(
                "visor.overlay.options.key_button.height"), startX + halfW + GAP, labelY, 0xFFFFFF);

        // ---- Row 2 ----
        labelY += ROW_SPACING;
        guiGraphics.drawString(font, Component.translatable(
                "visor.overlay.options.key_button.text"), startX, labelY, 0xFFFFFF);
        guiGraphics.drawString(font, Component.translatable(
                "visor.overlay.options.key_button.text_color"), startX + halfW + GAP, labelY, 0xFFFFFF);

        // ---- Row 3  ----
        labelY += ROW_SPACING;

        // ---- Row 4 ----
        labelY += ROW_SPACING;
        Component keyLabel = Component.translatable("visor.overlay.options.key_button.key");
        int keyLabelW = font.width(keyLabel);
        guiGraphics.drawString(font, keyLabel,
                startX + (fullW - keyLabelW) / 2, labelY, 0xFFFFFF);


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


}