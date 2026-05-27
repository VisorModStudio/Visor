package org.vmstudio.visor.core.client.gui.screens.settings.categories.controls;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.input.action.VRActionIdentifier;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoWidgetSetList;
import org.vmstudio.visor.api.client.gui.widgets.lists.WidgetSetList;
import org.vmstudio.visor.api.client.input.action.framework.VRActionKey;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.client.input.action.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRSettingsActions extends VROptionsSet {

    protected static final int ENTRY_GAP = 2;



    private WidgetSetList listWidget;
    private ButtonImaged profileButton;
    private ButtonImaged keyModifierButton;

    private ButtonImaged handButton;
    private ButtonImaged addActionButton;
    private ButtonImaged applyChangesButton;


    @Getter
    private final VRActionSet actionSet;

    @Getter
    private final Map<VRInteractionProfileType, Map<VRAction, ActionBinding>> newBindings;
    @Getter
    private final Map<VRInteractionProfileType, Boolean> newKeyModifiers;

    private final VRInteractionProfileType activeProfileType;
    @Getter
    private VRInteractionProfileType profileType;

    @Getter
    private boolean useLeftHanded;

    private boolean modified;
    private boolean canApplyChanges = true;


    public VRSettingsActions(@NotNull VRActionSet actionSet,
                             @NotNull VRSettingsScreen screen,
                             @Nullable VROptionsSet previousOptions,
                             @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
        this.actionSet = actionSet;
        this.activeProfileType = VisorState.get().isInitialized()
                ? ClientContext.inputManager.getActiveProfile()
                : null;
        this.profileType = activeProfileType != null
                ? activeProfileType
                : VRInteractionProfileType.OCULUS_TOUCH;
        this.useLeftHanded = VRClientSettings.isLeftHanded();
        this.newBindings = new EnumMap<>(VRInteractionProfileType.class);
        this.newKeyModifiers = new EnumMap<>(VRInteractionProfileType.class);
        resetNewBindings();
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
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {

        var scaleHelper = getScreen().getScaleHelper();

        Map<VRAction, ActionBinding> bindingMap = newBindings.get(profileType);

        listWidget = new WidgetSetList(
                new WidgetInfoWidgetSetList()
                        .pos(scaleHelper.scaledX(57), scaleHelper.scaledY(43))
                        .size(scaleHelper.scaledSize(142), scaleHelper.scaledSize(90))
                        .setEntryGap(ENTRY_GAP)
                        .setColumns(1)
                        .setEntryHeight(20)
                        .setTextureScrollBarActive(OptionTextures.SCROLL_BAR_ACTIVE),
                onWidgetsChanged
        );

        Map<String, VRActionEntry> listEntries = new LinkedHashMap<>();
        for(var entry : bindingMap.entrySet()){
            VRAction action = entry.getKey();
            ActionBinding binding = entry.getValue();
            var bindingKeyModifier = binding.getActionKeyModifier(useLeftHanded);
            var bindingId = binding.getActionId(useLeftHanded);
            var bindingDisplayName = binding.getActionDisplayName(useLeftHanded);
            var actionEntry = new VRActionEntry(action.getId(), action, onWidgetsChanged);
            actionEntry.bindingId = bindingId;
            actionEntry.bindingName = (bindingKeyModifier == ActionKeyModifierType.OFF
                    || bindingId.equals(ActionBinding.ID_EMPTY))
                    ? bindingDisplayName.getString()
                    : bindingKeyModifier.getDisplayName().getString() + " + "+ bindingDisplayName.getString();

            listEntries.put(action.getId(), actionEntry);
        }


        profileButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(119),scaleHelper.scaledY(31))
                        .size(scaleHelper.scaledSize(64), scaleHelper.scaledSize(8))
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                        .setTextScale(VRClientSettings.getSettingsTextScale())
                        .setText(Component.literal(
                                Component.translatable("visor.action.options.interaction_profile").getString()
                                + ": " + AtumColor.COLOR_SYMBOL+(activeProfileType == profileType
                                        ? "a"+ profileType.name() : "f"+ profileType.name())
                        )),
                (it)->{
                    nextProfilePressed();
                }
        );

        keyModifierButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(186),scaleHelper.scaledY(29))
                        .size(scaleHelper.scaledSize(12), scaleHelper.scaledSize(12))
                        .setTooltip(Tooltip.create(Component.translatable("visor.action.options.key_modifier_tooltip"))),
                (it)->{
                    switchKeyModifierPressed();
                }
        );



        addActionButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(65),scaleHelper.scaledY(136))
                        .size(scaleHelper.scaledSize(21), scaleHelper.scaledSize(10))
                        .setTexture(VRSettingsScreen.ADD_BUTTON)
                        .setTextScale(VRClientSettings.getSettingsTextScale())
                        .setTooltip(Tooltip.create(Component.translatable("visor.action.options.add_key_action_tooltip")))
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT),
                (it)->{
                    addActionPressed();
                }
        );

        applyChangesButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(96),scaleHelper.scaledY(136))
                        .size(scaleHelper.scaledSize(64), scaleHelper.scaledSize(10))
                        .setTexture(OptionTextures.LIGHT_GRAY_TEXTURE_2)
                        .setTextureInactive(OptionTextures.GRAY_TEXTURE)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                        .setTextScale(VRClientSettings.getSettingsTextScale())
                        .setText(Component.translatable("visor.button.apply_changes")),
                (it)->{
                    applyChangesPressed();
                }
        );

        handButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(170),scaleHelper.scaledY(136))
                        .size(scaleHelper.scaledSize(21), scaleHelper.scaledSize(10))
                        .setTooltip(Tooltip.create(Component.translatable("visor.action.options.hand.tooltip"))),
                (it)->{
                    switchHandPressed();
                }
        );

        if(newKeyModifiers.get(profileType)){
            keyModifierButton.getWidgetInfo()
                    .setTexture(VRSettingsScreen.KEY_MODIFIER_ON);
        }else {
            keyModifierButton.getWidgetInfo()
                    .setTexture(VRSettingsScreen.KEY_MODIFIER_OFF);
        }

        if(useLeftHanded){
            handButton.getWidgetInfo()
                    .setTexture(VRSettingsScreen.SWITCH_BUTTON_LEFT);
        }else {
            handButton.getWidgetInfo()
                    .setTexture(VRSettingsScreen.SWITCH_BUTTON_RIGHT);
        }


        modified = false;
        canApplyChanges = true;
        for(var entry : newBindings.entrySet()){
            if(actionSet.isKeyModifiersActive(entry.getKey())
                    != newKeyModifiers.get(entry.getKey())){
                modified = true;
            }
            for(var entry1 : entry.getValue().entrySet()){
                var action = entry1.getKey();
                var bindingOld = entry1.getKey()
                        .getBindingOrEmpty(entry.getKey());
                var bindingNew = entry1.getValue();
                if(!bindingOld.equals(bindingNew)){
                    modified = true;
                    var listEntry = listEntries.get(entry1.getKey().getId());
                    if(listEntry == null) continue;
                    var newKeyModifier = bindingNew.getActionKeyModifier(useLeftHanded);
                    var newId = bindingNew.getActionId(useLeftHanded);
                    var newIdDisplayName = bindingNew.getActionDisplayName(useLeftHanded);

                    var oldKeyModifier = bindingOld.getActionKeyModifier(useLeftHanded);
                    var oldActionId = bindingOld.getActionId(useLeftHanded);

                    if (!oldActionId.equals(newId) || oldKeyModifier != newKeyModifier) {
                        modified = true;
                        var listEntry1 = listEntries.get(entry1.getKey().getId());
                        if (listEntry1 == null) continue;

                        if (newKeyModifier != ActionKeyModifierType.OFF
                                && !newId.equals(ActionBinding.ID_EMPTY)) {
                            listEntry1.bindingName = AtumColor.COLOR_SYMBOL + "6"
                                    + newKeyModifier.getDisplayName().getString()
                                    + " + " + newIdDisplayName.getString();
                        } else {
                            if(newId.equals(ActionBinding.ID_EMPTY)){
                                if(action.isRequired()){
                                    canApplyChanges = false;
                                    listEntry1.bindingName = AtumColor.COLOR_SYMBOL + "c"
                                            + newIdDisplayName.getString();
                                    continue;
                                }
                                if(newId.equals(oldActionId)) {
                                    listEntry1.bindingName = newIdDisplayName.getString();
                                    continue;
                                }
                            }

                            listEntry1.bindingName = AtumColor.COLOR_SYMBOL + "6"
                                    + newIdDisplayName.getString();
                        }
                    }

                }
            }
        }

        listWidget.init(listEntries.values());

        updateApplyButton();


        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        list.add((T)listWidget);
        list.add((T) profileButton);
        list.add((T) keyModifierButton);
        list.add((T) addActionButton);
        list.add((T) applyChangesButton);
        list.add((T) handButton);
        return list;
    }

    @Override
    public void onPostRender(@NotNull GuiGraphics guiGraphics,
                             int mouseX, int mouseY,
                             float partialTicks) {
        var scaleHelper = getScreen().getScaleHelper();
        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                actionSet.getName().getString(),
                AtumColor.WHITE.asInt(),
                scaleHelper.scaledX(58), scaleHelper.scaledY(29),
                scaleHelper.scaledSize(58), scaleHelper.scaledSize(11),
                true
        );

        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                Component.translatable("visor.action.options.hand.left_handed").getString(),
                useLeftHanded
                        ? AtumColor.WHITE.asInt()
                        : AtumColor.DARK_GRAY.asInt(),
                scaleHelper.scaledX(171), scaleHelper.scaledY(137),
                scaleHelper.scaledSize(9), scaleHelper.scaledSize(8),
                true
        );
        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                Component.translatable("visor.action.options.hand.right_handed").getString(),
                !useLeftHanded
                        ? AtumColor.WHITE.asInt()
                        : AtumColor.DARK_GRAY.asInt(),
                scaleHelper.scaledX(181), scaleHelper.scaledY(137),
                scaleHelper.scaledSize(9), scaleHelper.scaledSize(8),
                true
        );
    }

    public Map<VRAction, ActionBinding> getNewBinds(){
        return newBindings.get(profileType);
    }

    public boolean isKeyModifiersActive(){
        return newKeyModifiers.get(profileType);
    }

    public void changeBinding(VRAction action,
                              ActionKeyModifierType keyModifierType,
                              VRActionIdentifier id){
        var bindingsMap = getNewBinds();
        var oldBinding = bindingsMap.get(action);
        if(oldBinding.getActionId(useLeftHanded).equals(id)
                && oldBinding.getActionKeyModifier(useLeftHanded).equals(keyModifierType)){
            return;
        }

        var newBinding =  new ActionBinding(
                oldBinding.getRightHandedKeyModifier(),
                oldBinding.getRightHandedId(),
                oldBinding.getLeftHandedKeyModifier(),
                oldBinding.getLeftHandedId()
        );

        newBinding.setActionId(id, useLeftHanded);
        newBinding.setActionKeyModifier(keyModifierType, useLeftHanded);

        bindingsMap.put(action, newBinding);

        if(id.equals(ActionBinding.ID_EMPTY)){
            return;
        }
        if(id.getValue().startsWith("vec2")){
            return; //ignore this for now, since some actions may use only 1 dimension
        }
        for(var entry : getNewBinds().entrySet().stream().toList()){
            if(entry.getKey() == action) continue;
            oldBinding = entry.getValue();
            var entryKeyModifier = oldBinding.getActionKeyModifier(useLeftHanded);
            var entryBindingId = oldBinding.getActionId(useLeftHanded);
            if(entryBindingId.equals(id) && entryKeyModifier == keyModifierType){
                newBinding =  new ActionBinding(
                        oldBinding.getRightHandedKeyModifier(),
                        oldBinding.getRightHandedId(),
                        oldBinding.getLeftHandedKeyModifier(),
                        oldBinding.getLeftHandedId()
                );
                newBinding.setActionId(ActionBinding.ID_EMPTY, useLeftHanded);
                newBinding.setActionKeyModifier(ActionKeyModifierType.OFF, useLeftHanded);
                bindingsMap.put(entry.getKey(), newBinding);
            }
        }

    }

    private List<VRInteractionProfileType> getProfiles() {
        if(!VisorState.get().isInitialized()){
            return List.of(VRInteractionProfileType.values());
        }
        return ClientContext.inputProvider.getSupportedProfileTypes();
    }


    private void resetNewBindings(){
        for(var profileType : VRInteractionProfileType.values()){
            newKeyModifiers.put(profileType, actionSet.isKeyModifiersActive(profileType));
        }

        newBindings.clear();
        for(var profile : VRInteractionProfileType.values()){
            var map = new LinkedHashMap<VRAction, ActionBinding>();
            actionSet.getActions().forEach(
                            a -> map.put(a, new ActionBinding(a.getBindingOrEmpty(profile)))
                    );
            newBindings.put(profile, map);
        }
    }

    public void addedKeyAction(@NotNull VRActionKey actionKey){
        newBindings.forEach((
                type,map) ->
                map.put(actionKey, new ActionBinding(ActionBinding.ID_EMPTY, ActionBinding.ID_EMPTY))
        );
    }
    public void removedKeyAction(@NotNull VRActionKey actionKey){
        newBindings.forEach((
                type,map) ->
                map.remove(actionKey)
        );
    }

    private void nextProfilePressed(){
        var profiles = getProfiles();
        int currentIndex = profiles.indexOf(profileType);
        int nextIndex = currentIndex + 1;
        if(nextIndex >= profiles.size()){
            nextIndex = 0;
        }
        profileType = profiles.get(nextIndex);
        reinit();
    }

    private void switchHandPressed(){
        useLeftHanded = !useLeftHanded;
        reinit();
    }

    private void switchKeyModifierPressed(){
        boolean flag = !newKeyModifiers.get(profileType);
        newKeyModifiers.put(
                profileType,
                flag
        );
        for(var entry : getNewBinds().entrySet()){
            var binding = entry.getValue();
            var supportedIds = entry.getKey()
                    .getSupportedBindingIds(profileType, isKeyModifiersActive());

            if(!flag && binding.getLeftHandedKeyModifier() != ActionKeyModifierType.OFF){
                binding.setLeftHandedKeyModifier(ActionKeyModifierType.OFF);
                binding.setLeftHandedId(ActionBinding.ID_EMPTY);
            }
            else if(!supportedIds.contains(binding.getLeftHandedId())){
                binding.setLeftHandedKeyModifier(ActionKeyModifierType.OFF);
                binding.setLeftHandedId(ActionBinding.ID_EMPTY);
            }

            if(!flag && binding.getRightHandedKeyModifier() != ActionKeyModifierType.OFF){
                binding.setRightHandedKeyModifier(ActionKeyModifierType.OFF);
                binding.setRightHandedId(ActionBinding.ID_EMPTY);
            }
            if(!supportedIds.contains(binding.getRightHandedId())){
                binding.setRightHandedKeyModifier(ActionKeyModifierType.OFF);
                binding.setRightHandedId(ActionBinding.ID_EMPTY);
            }
        }

        reinit();
    }

    private void applyChangesPressed(){
        var commonChanges = collectCommonChanges();
        if(!commonChanges.isEmpty()){
            getScreen().switchOptions(
                    new VRSettingsConfirmCrossBinding(
                            this, commonChanges, onWidgetsChanged
                    )
            );
            return;
        }
        commitStagedChanges();
    }

    public void commitStagedChanges(){
        newKeyModifiers.forEach(actionSet::setKeyModifiersActive);
        newBindings.forEach(
                (profile, map) ->
                        map.forEach((action, binding)-> action.setBinding(profile, binding))
        );

        actionSet.save();
        modified = false;
        resetNewBindings();
        reinit();
    }

    private List<CommonChange> collectCommonChanges(){
        var byAction = new LinkedHashMap<VRAction, Map<VRInteractionProfileType, ProfileChange>>();
        var allOtherSets = ClientContext.inputManager.getActionSetRegistry()
                .getSortedComponents()
                .stream()
                .filter(set -> set != actionSet)
                .toList();

        for(var profileEntry : newBindings.entrySet()){
            var profile = profileEntry.getKey();
            for(var bindingEntry : profileEntry.getValue().entrySet()){
                var action = bindingEntry.getKey();
                if(!action.isCommon()) continue;
                var newBinding = bindingEntry.getValue();
                var oldBinding = action.getBindingOrEmpty(profile);
                boolean rightChanged = !newBinding.getRightHandedId().equals(oldBinding.getRightHandedId())
                        || newBinding.getRightHandedKeyModifier() != oldBinding.getRightHandedKeyModifier();
                boolean leftChanged = !newBinding.getLeftHandedId().equals(oldBinding.getLeftHandedId())
                        || newBinding.getLeftHandedKeyModifier() != oldBinding.getLeftHandedKeyModifier();
                if(!rightChanged && !leftChanged) continue;

                byAction.computeIfAbsent(action, k -> new EnumMap<>(VRInteractionProfileType.class))
                        .put(profile, new ProfileChange(rightChanged, leftChanged, newBinding));
            }
        }

        List<CommonChange> result = new ArrayList<>();
        var destinationCache = new HashMap<String, List<VRActionSet>>();
        for(var entry : byAction.entrySet()){
            var action = entry.getKey();
            var destinations = destinationCache.computeIfAbsent(action.getId(), id ->
                    allOtherSets.stream()
                            .filter(set -> {
                                var a = set.getAction(id);
                                return a != null && a.isCommon();
                            })
                            .toList()
            );
            if(destinations.isEmpty()) continue;
            result.add(new CommonChange(action, entry.getValue(), destinations));
        }
        return result;
    }

    public record CommonChange(@NotNull VRAction action,
                               @NotNull Map<VRInteractionProfileType, ProfileChange> profileChanges,
                               @NotNull List<VRActionSet> destinations) {}

    public record ProfileChange(boolean rightChanged,
                                boolean leftChanged,
                                @NotNull ActionBinding newBinding) {}

    private void addActionPressed(){
        getScreen().switchOptions(
                new VRSettingsCreateKeyAction(this, onWidgetsChanged)
        );
    }


    private void updateApplyButton() {

        applyChangesButton.active = canApplyChanges && modified;
        if(applyChangesButton.active) {
            applyChangesButton.getWidgetInfo().setTextColor(AtumColor.YELLOW);
        }else{
            applyChangesButton.getWidgetInfo().setTextColor(VRSettingsScreen.INACTIVE_COLOR);
        }
    }


    private boolean hasBindingCollision() {
        for (boolean left : new boolean[]{false,true}) {
            var counts = getNewBinds().values().stream()
                    .map(b -> b.getActionId(left))
                    .filter(p -> !p.equals(ActionBinding.ID_EMPTY))
                    .filter(p->!p.getValue().startsWith("vec2")) //ignore this for now, since some actions may use only 1 dimension
                    .collect(Collectors.groupingBy(p -> p, Collectors.counting()));
            if (counts.values().stream().anyMatch(c -> c > 1)) return true;
        }
        return false;
    }

    @Override
    public void loadDefaults() {
        var map = newBindings.get(profileType);
        for(var action : actionSet.getActions()){
            var def = action.getDefaultBinding(profileType);
            if(def == null){
                def = ActionBinding.EMPTY;
            }
            map.put(action, new ActionBinding(def));
        }
        var keyModDefaults = actionSet.getDefaultKeyModifiersActive();
        for(var p : VRInteractionProfileType.values()){
            newKeyModifiers.put(p, keyModDefaults.getOrDefault(p, false));
        }
        applyChangesPressed();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        listWidget.mouseScrolled(mouseX, mouseY, delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Getter
    protected class VRActionEntry extends WidgetSetList.Entry {
        protected static final AtumColor NAME_COLOR = AtumColor.immutable(180,180,180,255);
        protected static final GuiTexture SEPARATOR_TEXTURE = TexturesHelper.getColorGuiTexture(
                AtumColor.immutable(116,116,116,255)
        );
        protected static final int SEPARATOR_HEIGHT = 1;

        private final VRAction action;
        @Setter
        private VRActionIdentifier bindingId;
        @Setter
        private String bindingName;

        private boolean keyAction;

        private int namePosX, namePosY, nameWidth, nameHeight;

        private int separatorPosX, separatorPosY, separatorWidth, separatorHeight;

        private ButtonImaged bindingButton;
        private ButtonImaged removeKeyActionButton;

        public VRActionEntry(@NotNull String id,
                             @NotNull VRAction action,
                             @NotNull Runnable onWidgetsChanged) {
            super(id, onWidgetsChanged);
            this.action = action;
        }


        @Override
        public <T extends GuiEventListener
                & Renderable
                & NarratableEntry> List<T> initWidgets() {

            keyAction = actionSet.getKeyAction(action.getId()) != null;

            bindingButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .setTexture(OptionTextures.GRAY_TEXTURE)
                            .setText(Component.literal(bindingName))
                            .setHighlightEnabled(true)
                            .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                            .setTextScale(VRClientSettings.getSettingsTextScale()),
                    it->{
                        getScreen().switchOptions(new VRSettingsActionBinding(
                                VRSettingsActions.this,
                                action,
                                onWidgetsChanged
                        ));
                    }
            );
            if(keyAction){
                removeKeyActionButton = new ButtonImaged(
                        new WidgetInfoButtonImaged()
                                .setTexture(VRSettingsScreen.REMOVE_KEY_ACTION)
                                .setHighlightEnabled(true)
                                .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT),
                        it->{
                            actionSet.removeKeyAction(action.getId());
                            removedKeyAction((VRActionKey) action);
                            reinit();
                        }
                );
            }

            return getWidgets();
        }

        @Override
        public <T extends GuiEventListener
                & Renderable
                & NarratableEntry> List<T> getWidgets() {
            List<T> list = new ArrayList<>();

            list.add((T)bindingButton);
            if(keyAction){
                list.add((T)removeKeyActionButton);
            }

            return list;
        }

        @Override
        public void onTick() {

        }

        @Override
        public void onPreRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            SEPARATOR_TEXTURE.blit(
                    guiGraphics,
                    separatorPosX,
                    separatorPosY,
                    separatorWidth,
                    separatorHeight
            );
            GuiHelper.renderScalableText(
                    guiGraphics,
                    MC.font,
                    action.getName().getString(),
                    NAME_COLOR.asInt(),
                    namePosX, namePosY,
                    nameWidth, nameHeight,
                    true
            );
        }

        @Override
        public void setLocation(int x, int y, int width, int height) {
            super.setLocation(x, y, width, height);
            separatorPosX = getX();
            separatorPosY = getY() + getHeight() - SEPARATOR_HEIGHT;
            separatorWidth = getWidth();
            separatorHeight = SEPARATOR_HEIGHT;

            namePosX = getX();
            namePosY = getY();
            nameWidth = getWidth() / 2 - 2;
            nameHeight = getHeight() - ENTRY_GAP - SEPARATOR_HEIGHT;

            bindingButton.setPosition(getX() + (getWidth() / 2) + 2, getY());
            bindingButton.setWidth(getWidth() / 2 - 2);
            bindingButton.height = getHeight() - ENTRY_GAP - SEPARATOR_HEIGHT;

            if(keyAction) {
                bindingButton.setWidth(getWidth() / 2 - 2 - 17);

                removeKeyActionButton.setPosition(getX() + (getWidth() / 2) + 2 + bindingButton.getWidth() + 2, getY());
                removeKeyActionButton.setWidth(15);
                removeKeyActionButton.height = getHeight() - ENTRY_GAP - SEPARATOR_HEIGHT;
            }
        }
    }

}
