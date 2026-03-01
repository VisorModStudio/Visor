package org.vmstudio.visor.core.client.gui.screens.settings.categories.controls;

import com.google.common.collect.Lists;
import me.phoenixra.atumvr.api.input.action.VRActionIdentifier;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.SliderWidget;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSelectionList;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSlider;
import org.vmstudio.visor.api.client.gui.widgets.lists.TexturedSelectionList;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.ActionKeyModifierType;
import org.vmstudio.visor.api.client.input.action.VisorAction;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.provider.openxr.XrProvider;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRSettingsActionBinding extends VROptionsSet {
    private final VRSettingsActions parent;
    private final VisorAction action;

    private TexturedSelectionList listWidget;
    private ButtonImaged captureInputButton;
    private ButtonImaged confirmButton;
    private ButtonImaged touchCheckbox;
    private ButtonImaged forceCheckbox;
    private SliderWidget<ActionKeyModifierType> keyModifiersSlider;


    private final ActionKeyModifierType initialKeyModifier;
    private ActionKeyModifierType selectedKeyModifier;

    private List<VRActionIdentifier> availableBindingIds;
    private final VRActionIdentifier initialBindingId;
    private VRActionIdentifier selectedBindingId;


    private boolean capturing = false;
    private long captureStart = 0L;

    private final boolean hasTouch;
    private final boolean hasForce;

    private boolean useTouch = false;
    private boolean useForce = false;


    public VRSettingsActionBinding(@NotNull VRSettingsActions parent,
                                   @NotNull VisorAction action,
                                   @NotNull Runnable onWidgetsChanged) {
        super(parent.getScreen(), parent, onWidgetsChanged);
        this.parent = parent;
        this.action = action;
        var binding = parent.getNewBinds()
                .get(action);
        this.initialKeyModifier = binding.getActionKeyModifier(parent.isUseLeftHanded());
        this.selectedKeyModifier = initialKeyModifier;
        this.initialBindingId = binding.getActionId(parent.isUseLeftHanded());
        this.selectedBindingId = initialBindingId;

        if(selectedBindingId.getValue().contains(".touch")){
            useTouch = true;
        }

        if(selectedBindingId.getValue().contains(".force")){
            useForce = true;
        }

        hasTouch = action.getSupportedBindingIds(parent.getProfileType(), parent.isKeyModifiersActive())
                .stream()
                .anyMatch(it-> it.getValue().contains(".touch"));
        hasForce = action.getSupportedBindingIds(parent.getProfileType(), parent.isKeyModifiersActive())
                .stream()
                .anyMatch(it-> it.getValue().contains(".force"));

        updateBindingsList();
    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return null;
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return null;
    }

    @Override
    protected boolean canLoadDefaults() {
        return false;
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {
        var scaleHelper = getScreen().getScaleHelper();

        Map<String, String> rawEntries = new LinkedHashMap<>();
        Map<String, VRActionIdentifier> entries = new HashMap<>();

        // To make bindings LEFT handed - RIGHT handed on list widget row,
        // without this they are reversed
        String name = AtumColor.COLOR_SYMBOL + "2";
        name += ActionBinding.getActionDisplayName(ActionBinding.ID_EMPTY).getString();
        rawEntries.put(ActionBinding.ID_EMPTY.getValue()+"_1", name);
        entries.put(ActionBinding.ID_EMPTY.getValue()+"_1", ActionBinding.ID_EMPTY);

        for(var bindingId : availableBindingIds){
            name = AtumColor.COLOR_SYMBOL;
            name += isBindingOccupied(bindingId) ? "6" : "2";
            name += ActionBinding.getActionDisplayName(bindingId).getString();
            rawEntries.put(bindingId.getValue(), name);
            entries.put(bindingId.getValue(), bindingId);
        }

        listWidget = new TexturedSelectionList(
                new WidgetInfoSelectionList()
                        .pos(scaleHelper.scaledX(57), scaleHelper.scaledY(43))
                        .size(scaleHelper.scaledSize(142), scaleHelper.scaledSize(90))
                        .setColumns(2)
                        .setEntryButton(
                                new WidgetInfoButtonImaged()
                                        .setTexture(OptionTextures.GRAY_TEXTURE)
                                        .highlight(
                                                OptionTextures.HOVERED_HIGHLIGHT,
                                                OptionTextures.SELECTED_HIGHLIGHT
                                        )
                        )
                        .setTextureScrollBarActive(OptionTextures.SCROLL_BAR_ACTIVE),
                rawEntries,
                it -> {
                    if(capturing) return;
                    if(it == null){
                        return;
                    }
                    VRActionIdentifier actionId = entries.get(it.getId());
                    if (actionId == null) return;
                    selectBinding(actionId);
                }
        );

        captureInputButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(148),scaleHelper.scaledY(31))
                        .size(scaleHelper.scaledSize(44), scaleHelper.scaledSize(8))
                        .setTexture(OptionTextures.LIGHT_GRAY_TEXTURE_2)
                        .setTextureInactive(OptionTextures.GRAY_TEXTURE)
                        .setTextScale(VRClientSettings.getSettingsTextScale())
                        .setText(Component.translatable("visor.action.options.bindings.capture_input"))
                        .setTooltip(Tooltip.create(Component.translatable("visor.action.options.bindings.capture_input.tooltip")))
                        .highlight(
                                OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT
                        ),
                (it)->{
                    captureInputPressed();
                }
        );

        touchCheckbox = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(60),scaleHelper.scaledY(135))
                        .size(scaleHelper.scaledSize(10), scaleHelper.scaledSize(10))
                        .setTexture(VRSettingsScreen.CHECKBOX_OFF)
                        .setTextureSelected(VRSettingsScreen.CHECKBOX_ON)
                        .setTooltip(Tooltip.create(Component.translatable("visor.action.options.bindings.touch_filter.tooltip")))
                        .setInactiveOnSelected(false)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT),
                (it)->{
                    if(capturing) return;
                    useTouch = !useTouch;
                    touchCheckbox.setSelected(useTouch);
                    updateBindingsList();
                    reinit();
                }
        );

        forceCheckbox = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(77),scaleHelper.scaledY(135))
                        .size(scaleHelper.scaledSize(10), scaleHelper.scaledSize(10))
                        .setTexture(VRSettingsScreen.CHECKBOX_OFF)
                        .setTextureSelected(VRSettingsScreen.CHECKBOX_ON)
                        .setTooltip(Tooltip.create(Component.translatable("visor.action.options.bindings.force_filter.tooltip")))
                        .setInactiveOnSelected(false)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT),
                (it)->{
                    if(capturing) return;
                    useForce = !useForce;
                    forceCheckbox.setSelected(useForce);
                    updateBindingsList();
                    reinit();
                }
        );


        keyModifiersSlider = new SliderWidget<>(
                new WidgetInfoSlider()
                        .pos(scaleHelper.scaledX(166),scaleHelper.scaledY(135))
                        .size(scaleHelper.scaledSize(26), scaleHelper.scaledSize(10))
                        .setBackgroundTexture(OptionTextures.GRAY_TEXTURE)
                        .setKnobTexture(OptionTextures.LIGHT_GRAY_TEXTURE_2)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT)
                        .setTooltip(Tooltip.create(Component.translatable("visor.action.options.bindings.key_modifier_tooltip")))
                        .setDynamicTextScale(true),
                Lists.newArrayList(ActionKeyModifierType.values()),
                (it)->{
                    keyModifierPressed(it.getSelected());

                }
        );

        confirmButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(94),scaleHelper.scaledY(136))
                        .size(scaleHelper.scaledSize(64), scaleHelper.scaledSize(10))
                        .setTexture(OptionTextures.LIGHT_GRAY_TEXTURE_2)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                        .setTextScale(VRClientSettings.getSettingsTextScale())
                        .setText(Component.translatable("visor.button.confirm")),
                (it)->{
                    confirmPressed();
                }
        );


        capturing = false;
        captureStart = 0L;


        listWidget.setSelectedEntry(selectedBindingId.getValue());

        if(parent.isKeyModifiersActive()){
            keyModifiersSlider.setSelected(
                    selectedKeyModifier,
                    false
            );
            keyModifiersSlider.setText(
                    selectedKeyModifier.getDisplayName()
            );
        }else{
            keyModifiersSlider.active = false;
            keyModifiersSlider.getWidgetInfo().setTextColor(VRSettingsScreen.INACTIVE_COLOR);
        }

        if(!hasTouch){
            touchCheckbox.active = false;
        }
        else if(useTouch){
            touchCheckbox.setSelected(true);
        }

        if(!hasForce){
            forceCheckbox.active = false;
        }
        else if(useForce){
            forceCheckbox.setSelected(true);
        }

        onTick();
        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        list.add((T) listWidget);
        list.add((T) captureInputButton);
        list.add((T) touchCheckbox);
        list.add((T) forceCheckbox);
        list.add((T) keyModifiersSlider);
        list.add((T) confirmButton);
        return list;
    }

    @Override
    public void onTick() {
        super.onTick();
        if(VisorState.get().isNotActive()){
            captureInputButton.active = false;
            captureInputButton.getWidgetInfo().setTextColor(VRSettingsScreen.INACTIVE_COLOR);
        }else {
            captureInputButton.active = true;
            captureInputButton.getWidgetInfo().setTextColor(AtumColor.WHITE);

        }
    }

    @Override
    public void onPostRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        var scaleHelper = getScreen().getScaleHelper();

        //Action name
        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                action.getName().getString(),
                AtumColor.WHITE.asInt(),
                scaleHelper.scaledX(58), scaleHelper.scaledY(29),
                scaleHelper.scaledSize(58), scaleHelper.scaledSize(11),
                true
        );

        //Touch
        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                Component.translatable("visor.action.options.bindings.touch_filter").getString(),
                hasTouch
                        ? AtumColor.WHITE.asInt()
                        : VRSettingsScreen.INACTIVE_COLOR.asInt(),
                scaleHelper.scaledX(59), scaleHelper.scaledY(146),
                scaleHelper.scaledY(11), scaleHelper.scaledY(5),
                true
        );

        //Force
        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                Component.translatable("visor.action.options.bindings.force_filter").getString(),
                hasForce
                        ? AtumColor.WHITE.asInt()
                        : VRSettingsScreen.INACTIVE_COLOR.asInt(),
                scaleHelper.scaledX(76), scaleHelper.scaledY(146),
                scaleHelper.scaledY(11), scaleHelper.scaledY(5),
                true
        );

        //Key modifier type
        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                Component.translatable("visor.action.options.bindings.key_modifier").getString(),
                keyModifiersSlider.active
                        ? AtumColor.WHITE.asInt()
                        : VRSettingsScreen.INACTIVE_COLOR.asInt(),
                scaleHelper.scaledX(165), scaleHelper.scaledY(146),
                scaleHelper.scaledY(27), scaleHelper.scaledY(5),
                true
        );
    }

    private void captureInputPressed(){

        if(VisorState.get().isNotActive()) return;

        capturing = true;
        captureStart = System.currentTimeMillis();

        var provider = (XrProvider) ClientContext.visor.getVrProvider();
        var inputHandler = provider.getInputHandler();
        inputHandler.setActionListener(
                (it)->{
                    if(captureStart + 500L > System.currentTimeMillis()){
                        return;
                    }
                    if(!availableBindingIds.contains(it)){
                        return;
                    }
                    selectedBindingId = it;
                    inputHandler.setActionListener(null);
                    reinit();
                    ClientContext.inputManager.setPausedActionsTicks(2);
                }
        );
        captureInputButton.setMessage(
                Component.translatable("visor.action.options.bindings.capture_input.listening")
        );
    }

    private void keyModifierPressed(ActionKeyModifierType keyModifier){
        this.selectedKeyModifier = keyModifier;
        keyModifiersSlider.setText(
                keyModifier.getDisplayName()
        );
        reinit();
    }

    private void confirmPressed(){
        parent.changeBinding(action, selectedKeyModifier, selectedBindingId);
        parent.getScreen().switchOptions(parent);
    }

    private void selectBinding(VRActionIdentifier id){
        selectedBindingId = id;
        updateConfirmColor();
    }

    private void updateBindingsList(){
        availableBindingIds = new ArrayList<>(action.getSupportedBindingIds(parent.getProfileType(), parent.isKeyModifiersActive()
                )
                .stream()
                .filter(it-> (useTouch || !it.getValue().contains(".touch"))
                        && (useForce || !it.getValue().contains(".force")))
                .toList());
    }

    private void updateConfirmColor(){
        if(initialBindingId.equals(selectedBindingId)){
            confirmButton.getWidgetInfo().setTextColor(AtumColor.WHITE);
        }else{
            confirmButton.getWidgetInfo().setTextColor(AtumColor.YELLOW);
        }
    }



    private boolean isBindingOccupied(VRActionIdentifier id){
        if(id.equals(ActionBinding.ID_EMPTY)){
            return false;
        }
        if(id.getValue().startsWith("vec2")){
            return false; //ignore this for now, since some actions may use only 1 dimension
        }
        for(var entry : parent.getNewBinds().entrySet()){
            if(entry.getKey() == action) continue;
            if(entry.getValue().getActionId(parent.isUseLeftHanded()).equals(id)
                    && entry.getValue().getActionKeyModifier(parent.isUseLeftHanded()) == selectedKeyModifier){
                return true;
            }
        }
        return false;

    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        listWidget.mouseScrolled(mouseX, mouseY, delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

}
