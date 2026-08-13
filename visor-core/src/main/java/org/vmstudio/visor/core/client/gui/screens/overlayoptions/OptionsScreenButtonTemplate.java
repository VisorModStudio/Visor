package org.vmstudio.visor.core.client.gui.screens.overlayoptions;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsVisibility;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.SliderWidget;
import org.vmstudio.visor.api.client.gui.widgets.color.ColorPickerWidgetSet;
import org.vmstudio.visor.api.client.gui.widgets.color.ColorSampleButton;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSelectionList;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSlider;
import org.vmstudio.visor.api.client.gui.widgets.lists.TexturedSelectionList;
import org.vmstudio.visor.api.client.input.action.VRAction;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.overlays.options.OverlayOptionsButtonTemplate;
import org.vmstudio.visor.core.client.gui.overlays.options.OverlayOptionsButtonTemplate.ActionType;
import org.vmstudio.visor.core.client.gui.overlays.options.OverlayOptionsButtonTemplate.VisibilityAction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//@TODO IT IS PROTOTYPE! REWORK FROM SCRATCH AFTER 0.7.0
public class OptionsScreenButtonTemplate extends OptionsScreen<OverlayOptionsButtonTemplate> {

    private static final String LANG = "visor.overlay.options.button_template.";

    private static final int FIELD_HEIGHT = 18;
    private static final int ROW_SPACING = 32;
    private static final int GAP = 6;


    private Page page = Page.MAIN;

    private ColorTarget colorTarget = ColorTarget.FILL;


    private EditBoxImaged widthField;
    private EditBoxImaged heightField;

    private EditBoxImaged buttonTextField;
    private ColorSampleButton textColorSample;

    private ButtonImaged visibilityButton;
    private SliderWidget<ActionType> actionTypeSlider;

    private EditBoxImaged keyField;
    private EditBoxImaged commandField;
    private ButtonImaged configureOverlaysButton;
    private ButtonImaged configureVrActionButton;

    private SliderWidget<OverlayOptionsButtonTemplate.CustomizationType> customizationTypeSlider;

    private ColorSampleButton colorSample;
    private ColorSampleButton hoverColorSample;

    private EditBoxImaged textureField;
    private EditBoxImaged hoverTextureField;

    private ColorPickerWidgetSet colorPicker;
    private ButtonImaged backButton;


    private TexturedSelectionList overlayList;
    private TexturedSelectionList actionSetList;
    private TexturedSelectionList vrActionList;

    // which action set the VR_ACTIONS page is listing
    private String browsedActionSetId;

    public OptionsScreenButtonTemplate(@NotNull OverlayOptionsButtonTemplate optionsGroup) {
        super(optionsGroup, Background.VERTICAL_WIDER);
    }

    @Override
    protected void onInit() {
        overlayList = null;
        actionSetList = null;
        vrActionList = null;
        switch (page) {
            case COLOR -> initColorPage();
            case OVERLAYS -> initOverlaysPage();
            case VR_ACTION_SETS -> initVrActionSetsPage();
            case VR_ACTIONS -> initVrActionsPage();
            default -> initMainPage();
        }
    }

    // MAIN page

    private void initMainPage() {

        int startX = cursorBoundsX + 10;
        int fullW = cursorBoundsWidth - 20;
        int halfW = (fullW - GAP) / 2;
        int rightX = startX + halfW + GAP;
        int y = cursorBoundsY + 22;

        // Row 1
        widthField = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(startX, y)
                        .size(halfW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable(LANG + "width"))
                        .setFilter(s -> s.matches("\\d*"))
        );
        widthField.setValue(String.valueOf(optionsGroup.getWidth()));
        widthField.setResponder(text -> {
            try {
                optionsGroup.setWidth(Integer.parseInt(text));
            } catch (NumberFormatException ignored) {
            }
        });

        heightField = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(rightX, y)
                        .size(halfW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable(LANG + "height"))
                        .setFilter(s -> s.matches("\\d*"))
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
                        .setHint(Component.translatable(LANG + "text"))
        );
        buttonTextField.setValue(optionsGroup.getText() != null ? optionsGroup.getText() : "");
        buttonTextField.setResponder(optionsGroup::setText);
        buttonTextField.setMaxLength(64);

        textColorSample = new ColorSampleButton(
                rightX, y,
                halfW, FIELD_HEIGHT,
                colorOf(ColorTarget.TEXT),
                it -> openColorPage(ColorTarget.TEXT)
        );

        // Row 3
        y += ROW_SPACING;

        visibilityButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(startX, y)
                        .size(halfW, FIELD_HEIGHT)
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

        actionTypeSlider = new SliderWidget<>(
                new WidgetInfoSlider()
                        .pos(rightX, y)
                        .size(halfW, FIELD_HEIGHT)
                        .setBackgroundTexture(OptionTextures.GRAY_TEXTURE)
                        .setKnobTexture(OptionTextures.LIGHT_GRAY_TEXTURE_2)
                        .setDynamicTextScale(true)
                        .setTextColor(AtumColor.WHITE),
                List.of(ActionType.values()),
                slider -> {
                    optionsGroup.setActionType(slider.getSelected());
                    slider.setText(actionText());
                    init();
                }
        );
        actionTypeSlider.setSelected(optionsGroup.getActionType(), false);
        actionTypeSlider.setText(actionText());

        // Row 4
        y += ROW_SPACING;

        // Row 5
        int modeW = 60;
        int modeX = startX + (fullW - modeW) / 2;
        int modeY = y + ROW_SPACING;

        customizationTypeSlider = new SliderWidget<>(
                new WidgetInfoSlider()
                        .pos(modeX, modeY)
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
        customizationTypeSlider.setSelected(optionsGroup.getCustomizationType(), false);
        customizationTypeSlider.setText(modeText());

        // Row 6
        int customizeY = modeY + ROW_SPACING;

        addRenderableWidget(widthField);
        addRenderableWidget(heightField);
        addRenderableWidget(buttonTextField);
        addRenderableWidget(textColorSample);
        addRenderableWidget(visibilityButton);
        addRenderableWidget(actionTypeSlider);
        addRenderableWidget(customizationTypeSlider);

        initActionControl(startX, y, fullW, halfW);

        if (optionsGroup.getCustomizationType()
                == OverlayOptionsButtonTemplate.CustomizationType.COLOR) {
            initColorFields(startX, customizeY, fullW, halfW, rightX);
        } else {
            initTextureFields(startX, customizeY, fullW);
        }
    }

    private void initActionControl(int startX, int y, int fullW, int halfW) {
        switch (optionsGroup.getActionType()) {
            case KEY -> {
                int keyFieldX = startX + (fullW - halfW) / 2;
                keyField = new EditBoxImaged(
                        new WidgetInfoEditBox()
                                .pos(keyFieldX, y)
                                .size(halfW, FIELD_HEIGHT)
                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                .setHint(Component.translatable(LANG + "key"))
                );
                keyField.setValue(optionsGroup.getKey());
                keyField.setResponder(optionsGroup::setKey);
                keyField.setMaxLength(32);
                addRenderableWidget(keyField);
            }
            case COMMAND -> {
                commandField = new EditBoxImaged(
                        new WidgetInfoEditBox()
                                .pos(startX, y)
                                .size(fullW, FIELD_HEIGHT)
                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                .setHint(Component.translatable(LANG + "command"))
                );
                commandField.setValue(optionsGroup.getCommand() != null ? optionsGroup.getCommand() : "");
                commandField.setResponder(optionsGroup::setCommand);
                commandField.setMaxLength(256);
                addRenderableWidget(commandField);
            }
            case OVERLAY_VISIBILITY -> {
                configureOverlaysButton = new ButtonImaged(
                        new WidgetInfoButtonImaged()
                                .pos(startX, y)
                                .size(fullW, FIELD_HEIGHT)
                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                .setText(Component.translatable(LANG + "configure_overlays"))
                                .highlight(
                                        OptionTextures.HOVERED_HIGHLIGHT,
                                        OptionTextures.SELECTED_HIGHLIGHT
                                ),
                        button -> {
                            page = Page.OVERLAYS;
                            init();
                        }
                );
                addRenderableWidget(configureOverlaysButton);
            }
            case VR_ACTION -> {
                configureVrActionButton = new ButtonImaged(
                        new WidgetInfoButtonImaged()
                                .pos(startX, y)
                                .size(fullW, FIELD_HEIGHT)
                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                .setText(vrActionText())
                                .setTooltip(Tooltip.create(
                                        Component.translatable(LANG + "vr_action.tooltip")
                                ))
                                .highlight(
                                        OptionTextures.HOVERED_HIGHLIGHT,
                                        OptionTextures.SELECTED_HIGHLIGHT
                                ),
                        button -> {
                            page = Page.VR_ACTION_SETS;
                            init();
                        }
                );
                addRenderableWidget(configureVrActionButton);
            }
        }
    }

    private void initColorFields(int baseX, int y, int fullW, int halfW, int rightX) {
        colorSample = new ColorSampleButton(
                baseX, y,
                halfW, FIELD_HEIGHT,
                colorOf(ColorTarget.FILL),
                it -> openColorPage(ColorTarget.FILL)
        );
        hoverColorSample = new ColorSampleButton(
                rightX, y,
                halfW, FIELD_HEIGHT,
                colorOf(ColorTarget.HOVER),
                it -> openColorPage(ColorTarget.HOVER)
        );
        addRenderableWidget(colorSample);
        addRenderableWidget(hoverColorSample);
    }

    private void initTextureFields(int baseX, int y, int fieldW) {
        textureField = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(baseX, y)
                        .size(fieldW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable(LANG + "texture"))
        );
        textureField.setValue(optionsGroup.getRawTexturePath() != null
                ? optionsGroup.getRawTexturePath()
                : ""
        );
        textureField.setResponder(optionsGroup::setTexturePath);
        textureField.setMaxLength(256);
        addRenderableWidget(textureField);

        hoverTextureField = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(baseX, y + ROW_SPACING)
                        .size(fieldW, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable(LANG + "hover_texture"))
        );
        hoverTextureField.setValue(optionsGroup.getRawHoverTexturePath() != null
                ? optionsGroup.getRawHoverTexturePath()
                : ""
        );
        hoverTextureField.setResponder(optionsGroup::setHoverTexturePath);
        hoverTextureField.setMaxLength(256);
        addRenderableWidget(hoverTextureField);
    }

    // COLOR page

    private void initColorPage() {

        int startX = cursorBoundsX + 10;
        int fullW = cursorBoundsWidth - 20;
        int pickerY = cursorBoundsY + 12 + 14;

        colorPicker = new ColorPickerWidgetSet(
                startX, pickerY, fullW,
                colorOf(colorTarget),
                colorTarget != ColorTarget.TEXT,
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

    // OVERLAYS page

    private void initOverlaysPage() {

        int startX = cursorBoundsX + 10;
        int fullW = cursorBoundsWidth - 20;
        int listY = cursorBoundsY + 12 + 16;
        int backY = cursorBoundsY + cursorBoundsHeight - 12 - FIELD_HEIGHT;
        int listHeight = Math.max(FIELD_HEIGHT, backY - listY - GAP - 2);

        Map<String, String> rows = buildOverlayRows();
        overlayList = null;
        if (!rows.isEmpty()) {
            overlayList = new TexturedSelectionList(
                    new WidgetInfoSelectionList()
                            .pos(startX, listY)
                            .size(fullW, listHeight)
                            .setColumns(1)
                            .setEntryButton(
                                    new WidgetInfoButtonImaged()
                                            .setTexture(OptionTextures.GRAY_TEXTURE)
                                            .highlight(
                                                    OptionTextures.HOVERED_HIGHLIGHT,
                                                    OptionTextures.SELECTED_HIGHLIGHT
                                            )
                            )
                            .setTextureScrollBarActive(OptionTextures.SCROLL_BAR_ACTIVE),
                    rows,
                    entry -> {
                        if (entry == null) return;
                        cycleOverlayState(entry.getId());
                        overlayList.renameEntry(entry.getId(), overlayRowLabel(entry.getId()));
                        overlayList.clearSelection();
                    }
            );
            addRenderableWidget(overlayList);
        }

        backButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(startX, backY)
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

    private Map<String, String> buildOverlayRows() {
        Map<String, String> rows = new LinkedHashMap<>();
        VROverlay self = optionsGroup.getOwner();
        for (VROverlay overlay : ClientContext.overlayManager.getOverlaysRegistry().getSortedByName()) {
            if (overlay == self) continue;
            if (overlay.getOption(OverlayOptionsVisibility.ID, OverlayOptionsVisibility.class) == null) {
                continue;
            }
            rows.put(overlay.getId(), overlayRowLabel(overlay.getId()).getString());
        }
        return rows;
    }

    private Component overlayRowLabel(@NotNull String id) {
        VROverlay overlay = ClientContext.overlayManager.getOverlay(id);
        String name = overlay != null ? overlay.getName().getString() : id;
        return Component.literal(name + " — ")
                .append(stateLabel(optionsGroup.getOverlayAction(id)));
    }

    private Component stateLabel(@Nullable VisibilityAction action) {
        String key = action == null ? "none" : action.name().toLowerCase();
        return Component.translatable(LANG + "overlays.state." + key);
    }

    // VR_ACTION_SETS page

    private void initVrActionSetsPage() {

        int startX = cursorBoundsX + 10;
        int fullW = cursorBoundsWidth - 20;
        int listY = cursorBoundsY + 12 + 16;
        int backY = cursorBoundsY + cursorBoundsHeight - 12 - FIELD_HEIGHT;
        int listHeight = Math.max(FIELD_HEIGHT, backY - listY - GAP - 2);

        Map<String, String> rows = buildVrActionSetRows();
        if (!rows.isEmpty()) {
            actionSetList = new TexturedSelectionList(
                    new WidgetInfoSelectionList()
                            .pos(startX, listY)
                            .size(fullW, listHeight)
                            .setColumns(1)
                            .setTooltip(this::actionSetTooltip)
                            .setEntryButton(
                                    new WidgetInfoButtonImaged()
                                            .setTexture(OptionTextures.GRAY_TEXTURE)
                                            .highlight(
                                                    OptionTextures.HOVERED_HIGHLIGHT,
                                                    OptionTextures.SELECTED_HIGHLIGHT
                                            )
                            )
                            .setTextureScrollBarActive(OptionTextures.SCROLL_BAR_ACTIVE),
                    rows,
                    entry -> {
                        if (entry == null) return;
                        browsedActionSetId = entry.getId();
                        page = Page.VR_ACTIONS;
                        init();
                    }
            );
            addRenderableWidget(actionSetList);
        }

        backButton = backButton(startX, backY, fullW, Page.MAIN);
        addRenderableWidget(backButton);
    }

    private Map<String, String> buildVrActionSetRows() {
        Map<String, String> rows = new LinkedHashMap<>();
        for (VRActionSet set : ClientContext.inputManager
                .getActionSetRegistry().getSortedComponents()) {
            if (OverlayOptionsButtonTemplate.getSelectableActions(set).isEmpty()) {
                continue;
            }
            rows.put(set.getId(), set.getName().getString());
        }
        return rows;
    }

    @Nullable
    private Component actionSetTooltip(@NotNull String setId) {
        VRActionSet set = ClientContext.inputManager
                .getActionSetRegistry().getComponent(setId);
        return set != null ? set.getTooltip() : null;
    }

    // VR_ACTIONS page

    private void initVrActionsPage() {

        int startX = cursorBoundsX + 10;
        int fullW = cursorBoundsWidth - 20;
        int listY = cursorBoundsY + 12 + 16;
        int backY = cursorBoundsY + cursorBoundsHeight - 12 - FIELD_HEIGHT;
        int listHeight = Math.max(FIELD_HEIGHT, backY - listY - GAP - 2);

        Map<String, String> rows = buildVrActionRows(browsedActionSet());
        if (!rows.isEmpty()) {
            vrActionList = new TexturedSelectionList(
                    new WidgetInfoSelectionList()
                            .pos(startX, listY)
                            .size(fullW, listHeight)
                            .setColumns(1)
                            .setEntryButton(
                                    new WidgetInfoButtonImaged()
                                            .setTexture(OptionTextures.GRAY_TEXTURE)
                                            .highlight(
                                                    OptionTextures.HOVERED_HIGHLIGHT,
                                                    OptionTextures.SELECTED_HIGHLIGHT
                                            )
                            )
                            .setTextureScrollBarActive(OptionTextures.SCROLL_BAR_ACTIVE),
                    rows,
                    entry -> {
                        if (entry == null) return;
                        optionsGroup.setVrAction(browsedActionSetId, entry.getId());
                        page = Page.MAIN;
                        init();
                    }
            );
            addRenderableWidget(vrActionList);
        }

        backButton = backButton(startX, backY, fullW, Page.VR_ACTION_SETS);
        addRenderableWidget(backButton);
    }

    private Map<String, String> buildVrActionRows(@Nullable VRActionSet set) {
        Map<String, String> rows = new LinkedHashMap<>();
        if (set == null) {
            return rows;
        }
        for (VRActionButton action : OverlayOptionsButtonTemplate.getSelectableActions(set)) {
            rows.put(action.getId(), action.getName().getString());
        }
        return rows;
    }

    @Nullable
    private VRActionSet browsedActionSet() {
        if (browsedActionSetId == null || browsedActionSetId.isEmpty()) {
            return null;
        }
        return ClientContext.inputManager
                .getActionSetRegistry().getComponent(browsedActionSetId);
    }

    private ButtonImaged backButton(int x, int y, int width, @NotNull Page target) {
        return new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(x, y)
                        .size(width, FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setText(Component.translatable("visor.button.back"))
                        .highlight(
                                OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT
                        ),
                button -> {
                    page = target;
                    init();
                }
        );
    }

    private Component vrActionText() {
        String setId = optionsGroup.getVrActionSetId();
        String actionId = optionsGroup.getVrActionId();
        if (setId.isEmpty() || actionId.isEmpty()) {
            return Component.translatable(LANG + "vr_action.select");
        }

        VRActionSet set = ClientContext.inputManager
                .getActionSetRegistry().getComponent(setId);
        VRAction action = set != null ? set.getAction(actionId) : null;
        if (action == null) {
            return Component.translatable(LANG + "vr_action.unknown");
        }
        return Component.literal(
                set.getName().getString() + ": " + action.getName().getString()
        );
    }

    private void cycleOverlayState(@NotNull String id) {
        VisibilityAction current = optionsGroup.getOverlayAction(id);
        VisibilityAction next;
        if (current == null) {
            next = VisibilityAction.TOGGLE;
        } else {
            next = switch (current) {
                case TOGGLE -> VisibilityAction.SHOW;
                case SHOW -> VisibilityAction.HIDE;
                case HIDE -> null;
            };
        }
        optionsGroup.setOverlayAction(id, next);
    }

    // Lifecycle

    @Override
    public void tick() {
        super.tick();
        if (page == Page.COLOR && colorPicker != null) {
            colorPicker.onTick();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double delta) {
        if (page == Page.OVERLAYS && overlayList != null) {
            return overlayList.mouseScrolled(mouseX, mouseY, scrollX, delta);
        }
        if (page == Page.VR_ACTION_SETS && actionSetList != null) {
            return actionSetList.mouseScrolled(mouseX, mouseY, scrollX, delta);
        }
        if (page == Page.VR_ACTIONS && vrActionList != null) {
            return vrActionList.mouseScrolled(mouseX, mouseY, scrollX, delta);
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, delta);
    }

    @Override
    protected void onRender(GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTick) {
        switch (page) {
            case COLOR -> renderColorPage(guiGraphics, mouseX, mouseY, partialTick);
            case OVERLAYS -> renderOverlaysPage(guiGraphics);
            case VR_ACTION_SETS -> renderVrActionSetsPage(guiGraphics);
            case VR_ACTIONS -> renderVrActionsPage(guiGraphics);
            default -> renderMainPage(guiGraphics);
        }
    }

    // Color helpers

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

        if (target == ColorTarget.HOVER) {
            AtumColor hover = optionsGroup.getHoverColor();
            if (hover != null) {
                return hover;
            }
            AtumColor base = optionsGroup.getColor();
            if (base == null) {
                base = AtumColor.WHITE;
            }
            return AtumColor.immutable(
                    base.getRedInt(), base.getGreenInt(), base.getBlueInt(), 0
            );
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
        switch (colorTarget) {
            case FILL -> optionsGroup.setColor(color);
            case TEXT -> optionsGroup.setTextColor(color);
            case HOVER -> optionsGroup.setHoverColor(color);
        }
    }

    // Rendering

    private void renderMainPage(GuiGraphics guiGraphics) {

        int startX = cursorBoundsX + 10;
        int fullW = cursorBoundsWidth - 20;
        int halfW = (fullW - GAP) / 2;
        int rightX = startX + halfW + GAP;
        int labelY = cursorBoundsY + 13;

        // Row 1
        guiGraphics.drawString(font, Component.translatable(LANG + "width"),
                startX, labelY, 0xFFFFFF);
        guiGraphics.drawString(font, Component.translatable(LANG + "height"),
                rightX, labelY, 0xFFFFFF);

        // Row 2
        labelY += ROW_SPACING;
        guiGraphics.drawString(font, Component.translatable(LANG + "text"),
                startX, labelY, 0xFFFFFF);
        guiGraphics.drawString(font, Component.translatable(LANG + "text_color"),
                rightX, labelY, 0xFFFFFF);

        // Row 3

        // Row 4
        labelY += ROW_SPACING * 2;
        if (optionsGroup.getActionType() == ActionType.KEY) {
            Component keyLabel = Component.translatable(LANG + "key");
            int keyLabelW = font.width(keyLabel);
            guiGraphics.drawString(font, keyLabel,
                    startX + (fullW - keyLabelW) / 2, labelY, 0xFFFFFF);
        } else if (optionsGroup.getActionType() == ActionType.COMMAND) {
            guiGraphics.drawString(font, Component.translatable(LANG + "command"),
                    startX, labelY, 0xFFFFFF);
        }

        // Row 6
        labelY += ROW_SPACING * 2;
        if (optionsGroup.getCustomizationType()
                == OverlayOptionsButtonTemplate.CustomizationType.COLOR) {
            guiGraphics.drawString(font, Component.translatable(LANG + "fill_color"),
                    startX, labelY, 0xFFFFFF);
            guiGraphics.drawString(font, Component.translatable(LANG + "hover_color"),
                    rightX, labelY, 0xFFFFFF);
        } else {
            guiGraphics.drawString(font, Component.translatable(LANG + "texture"),
                    startX, labelY, 0xFFFFFF);
            guiGraphics.drawString(font, Component.translatable(LANG + "hover_texture"),
                    startX, labelY + ROW_SPACING, 0xFFFFFF);
        }
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

    private void renderOverlaysPage(GuiGraphics guiGraphics) {
        int startX = cursorBoundsX + 10;
        int titleY = cursorBoundsY + 12 + 3;

        guiGraphics.drawString(font, Component.translatable(LANG + "overlays.title"),
                startX, titleY, 0xFFFFFF);

        if (overlayList == null) {
            guiGraphics.drawString(font, Component.translatable(LANG + "overlays.empty"),
                    startX, cursorBoundsY + cursorBoundsHeight / 2, 0xFFAAAAAA);
        }
    }

    private void renderVrActionSetsPage(GuiGraphics guiGraphics) {
        int startX = cursorBoundsX + 10;
        int titleY = cursorBoundsY + 12 + 3;

        guiGraphics.drawString(font, Component.translatable(LANG + "vr_action_sets.title"),
                startX, titleY, 0xFFFFFF);

        if (actionSetList == null) {
            guiGraphics.drawString(font, Component.translatable(LANG + "vr_action_sets.empty"),
                    startX, cursorBoundsY + cursorBoundsHeight / 2, 0xFFAAAAAA);
        }
    }

    private void renderVrActionsPage(GuiGraphics guiGraphics) {
        int startX = cursorBoundsX + 10;
        int titleY = cursorBoundsY + 12 + 3;

        VRActionSet set = browsedActionSet();
        guiGraphics.drawString(font,
                set != null ? set.getName() : Component.translatable(LANG + "vr_actions.title"),
                startX, titleY, 0xFFFFFF);

        if (vrActionList == null) {
            guiGraphics.drawString(font, Component.translatable(LANG + "vr_actions.empty"),
                    startX, cursorBoundsY + cursorBoundsHeight / 2, 0xFFAAAAAA);
        }
    }

    // Text helpers

    private Component visibilityText() {
        return Component.translatable(
                LANG + "visible",
                Component.translatable(optionsGroup.isWorldOnly()
                        ? LANG + "visible.world"
                        : LANG + "visible.always")
        );
    }

    private Component actionText() {
        String key = switch (optionsGroup.getActionType()) {
            case KEY -> "key";
            case COMMAND -> "command";
            case OVERLAY_VISIBILITY -> "overlay_visibility";
            case VR_ACTION -> "vr_action";
        };
        return Component.translatable(LANG + "action",
                Component.translatable(LANG + "action." + key));
    }

    private Component modeText() {
        return Component.translatable(
                LANG + "mode",
                optionsGroup.getCustomizationType().name()
        );
    }


    private enum Page {
        MAIN,
        COLOR,
        OVERLAYS,
        VR_ACTION_SETS,
        VR_ACTIONS
    }


    private enum ColorTarget {
        FILL(LANG + "color"),
        TEXT(LANG + "text_color"),
        HOVER(LANG + "hover_color");

        private final String titleKey;

        ColorTarget(String titleKey) {
            this.titleKey = titleKey;
        }

        Component title() {
            return Component.translatable(titleKey);
        }
    }
}
