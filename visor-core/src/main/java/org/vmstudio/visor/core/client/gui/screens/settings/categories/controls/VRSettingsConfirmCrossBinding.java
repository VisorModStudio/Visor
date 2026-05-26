package org.vmstudio.visor.core.client.gui.screens.settings.categories.controls;

import me.phoenixra.atumvr.api.input.action.VRActionIdentifier;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoCheckboxList;
import org.vmstudio.visor.api.client.gui.widgets.lists.CheckboxList;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.ActionKeyModifierType;
import org.vmstudio.visor.api.client.input.action.VRAction;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.SettingsTextures;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRSettingsConfirmCrossBinding extends VROptionsSet {
    private static final AtumColor HEADER_COLOR = AtumColor.immutable(235, 235, 230, 255);
    private static final AtumColor COUNTER_COLOR = AtumColor.immutable(130, 130, 125, 255);
    private static final AtumColor ACTION_COLOR = AtumColor.immutable(255, 200, 80, 255);
    private static final AtumColor SUMMARY_COLOR = AtumColor.immutable(175, 175, 170, 255);
    private static final AtumColor BUTTON_TEXT_COLOR = AtumColor.immutable(190, 190, 185, 255);

    private final VRSettingsActions parent;
    private final List<VRSettingsActions.CommonChange> queue;
    private final Map<VRAction, List<String>> decisions = new LinkedHashMap<>();

    private int currentIndex = 0;

    private CheckboxList destinationList;
    private ButtonImaged checkboxAllButton;
    private ButtonImaged applyButton;
    private ButtonImaged skipButton;
    private ButtonImaged cancelButton;

    public VRSettingsConfirmCrossBinding(@NotNull VRSettingsActions parent,
                                         @NotNull List<VRSettingsActions.CommonChange> queue,
                                         @NotNull Runnable onWidgetsChanged) {
        super(parent.getScreen(), parent, onWidgetsChanged);
        this.parent = parent;
        this.queue = queue;
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
        var sh = getScreen().getScaleHelper();
        var current = queue.get(currentIndex);

        Map<String, String> entries = new LinkedHashMap<>();
        for (var set : current.destinations()) {
            entries.put(set.getId(), set.getName().getString());
        }

        destinationList = new CheckboxList(
                new WidgetInfoCheckboxList()
                        .pos(sh.scaledX(58 + 35), sh.scaledY(77))
                        .size(sh.scaledSize(140 - 70), sh.scaledSize(43))
                        .textures(
                                OptionTextures.GRAY_TEXTURE,
                                SettingsTextures.CHECKBOX_BUTTON,
                                SettingsTextures.CHECKBOX_BUTTON_HOVERED,
                                SettingsTextures.CHECKBOX_BUTTON_SELECTED,
                                SettingsTextures.CHECKBOX_BUTTON_HOVERED_SELECTED
                        )
                        .setItemHeight(sh.scaledSize(11))
                        .setTextColor(AtumColor.WHITE),
                entries,
                new ArrayList<>(decisions.getOrDefault(current.action(), List.of())),
                (entry) -> checkboxAllButton.setSelected(destinationList.isAllSelected())
        );

        checkboxAllButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(sh.scaledX(57 + 35), sh.scaledY(64))
                        .size(sh.scaledSize(11), sh.scaledSize(11))
                        .textures(
                                SettingsTextures.CHECKBOX_BUTTON,
                                SettingsTextures.CHECKBOX_BUTTON_HOVERED,
                                SettingsTextures.CHECKBOX_BUTTON_SELECTED,
                                SettingsTextures.CHECKBOX_BUTTON_HOVERED_SELECTED,
                                null
                        )
                        .setInactiveOnSelected(false),
                (it) -> {
                    boolean next = !it.isSelected();
                    it.setSelected(next);
                    destinationList.changeSelectedAll(next);
                }
        );
        //select all by default
        destinationList.changeSelectedAll(true);

        applyButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(sh.scaledX(58), sh.scaledY(124))
                        .size(sh.scaledSize(44), sh.scaledSize(11))
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                        .setTextScale(VRClientSettings.getSettingsTextScale())
                        .setTextColor(AtumColor.YELLOW)
                        .setText(Component.translatable("visor.action.options.bindings.cross.apply")),
                (it) -> applyPressed()
        );

        skipButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(sh.scaledX(106), sh.scaledY(124))
                        .size(sh.scaledSize(44), sh.scaledSize(11))
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                        .setTextScale(VRClientSettings.getSettingsTextScale())
                        .setText(Component.translatable("visor.action.options.bindings.cross.skip"))
                        .setTextColor(BUTTON_TEXT_COLOR),
                (it) -> skipPressed()
        );

        cancelButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(sh.scaledX(154), sh.scaledY(124))
                        .size(sh.scaledSize(44), sh.scaledSize(11))
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                        .setTextScale(VRClientSettings.getSettingsTextScale())
                        .setText(Component.translatable("visor.button.cancel"))
                        .setTextColor(BUTTON_TEXT_COLOR),
                (it) -> cancelPressed()
        );

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        list.add((T) destinationList);
        list.add((T) checkboxAllButton);
        list.add((T) applyButton);
        list.add((T) skipButton);
        list.add((T) cancelButton);
        return list;
    }

    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {
        var sh = getScreen().getScaleHelper();
        OptionTextures.GRAY_TEXTURE.blit(
                guiGraphics,
                sh.scaledX(88), sh.scaledY(60),
                sh.scaledSize(80), sh.scaledSize(62)
        );
    }

    @Override
    public void onPostRender(@NotNull GuiGraphics guiGraphics,
                             int mouseX, int mouseY,
                             float partialTicks) {
        var sh = getScreen().getScaleHelper();
        var current = queue.get(currentIndex);

        // Header
        GuiHelper.renderScalableText(
                guiGraphics, MC.font,
                Component.translatable("visor.action.options.bindings.cross.title").getString(),
                HEADER_COLOR.asInt(),
                sh.scaledX(58), sh.scaledY(29),
                sh.scaledSize(142), sh.scaledSize(10),
                true
        );

        // Counter
        if (queue.size() > 1) {
            GuiHelper.renderScalableText(
                    guiGraphics, MC.font,
                    Component.translatable(
                            "visor.action.options.bindings.cross.counter",
                            currentIndex + 1, queue.size()
                    ).getString(),
                    COUNTER_COLOR.asInt(),
                    sh.scaledX(176), sh.scaledY(29),
                    sh.scaledSize(22), sh.scaledSize(10),
                    true
            );
        }

        // Action name
        GuiHelper.renderScalableText(
                guiGraphics, MC.font,
                current.action().getName().getString(),
                ACTION_COLOR.asInt(),
                sh.scaledX(58), sh.scaledY(42),
                sh.scaledSize(142), sh.scaledSize(9),
                true
        );

        // Binding summary
        GuiHelper.renderScalableText(
                guiGraphics, MC.font,
                summarizeBinding(current),
                SUMMARY_COLOR.asInt(),
                sh.scaledX(58), sh.scaledY(53),
                sh.scaledSize(142), sh.scaledSize(6),
                true
        );

        //All checkbox label
        GuiHelper.renderScalableText(
                guiGraphics, MC.font,
                Component.translatable("visor.action.options.bindings.cross.all").getString(),
                AtumColor.WHITE.asInt(),
                sh.scaledX(106), sh.scaledY(64),
                sh.scaledSize(10), sh.scaledSize(11),
                true
        );
    }

    private String summarizeBinding(@NotNull VRSettingsActions.CommonChange change) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (var entry : change.profileChanges().entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(entry.getKey().name());
            sb.append(" ");
            var profileChange = entry.getValue();
            var newBinding = profileChange.newBinding();
            if (profileChange.rightChanged()) {
                sb.append("R:").append(formatHand(newBinding.getRightHandedId(), newBinding.getRightHandedKeyModifier()));
            }
            if (profileChange.leftChanged()) {
                if (profileChange.rightChanged()) sb.append(" ");
                sb.append("L:").append(formatHand(newBinding.getLeftHandedId(), newBinding.getLeftHandedKeyModifier()));
            }
        }
        return sb.toString();
    }

    private String formatHand(@NotNull VRActionIdentifier id, @NotNull ActionKeyModifierType mod) {
        var name = ActionBinding.getActionDisplayName(id).getString();
        if (mod == ActionKeyModifierType.OFF || id.equals(ActionBinding.ID_EMPTY)) {
            return name;
        }
        return mod.getDisplayName().getString() + "+" + name;
    }

    private void applyPressed() {
        var current = queue.get(currentIndex);
        decisions.put(current.action(), new ArrayList<>(destinationList.getSelectedEntriesId()));
        advance();
    }

    private void skipPressed() {
        var current = queue.get(currentIndex);
        decisions.put(current.action(), new ArrayList<>());
        advance();
    }

    private void cancelPressed() {
        getScreen().switchOptions(parent);
    }

    private void advance() {
        currentIndex++;
        if (currentIndex >= queue.size()) {
            finish();
            return;
        }
        reinit();
    }

    private void finish() {
        for (var change : queue) {
            var destIds = decisions.get(change.action());
            if (destIds == null || destIds.isEmpty()) continue;
            for (var destSet : change.destinations()) {
                if (!destIds.contains(destSet.getId())) continue;
                applyToDestination(destSet, change);
                destSet.save();
            }
        }
        parent.commitStagedChanges();
        getScreen().switchOptions(parent);
    }

    private void applyToDestination(@NotNull VRActionSet destSet,
                                    @NotNull VRSettingsActions.CommonChange change) {
        var destAction = destSet.getAction(change.action().getId());
        if (destAction == null) return;

        for (var pcEntry : change.profileChanges().entrySet()) {
            var profile = pcEntry.getKey();
            var profileChange = pcEntry.getValue();
            var newBinding = profileChange.newBinding();

            var destSupported = destAction.getSupportedBindingIds(
                    profile,
                    destSet.isKeyModifiersActive(profile)
            );
            var oldDestBinding = destAction.getBindingOrEmpty(profile);
            var updatedBinding = new ActionBinding(oldDestBinding);

            if (profileChange.rightChanged()) {
                var newRightId = newBinding.getRightHandedId();
                var newRightMod = newBinding.getRightHandedKeyModifier();
                if (newRightId.equals(ActionBinding.ID_EMPTY) || destSupported.contains(newRightId)) {
                    avoidCollisionInSet(destSet, destAction, profile, newRightId, newRightMod, false);
                    updatedBinding.setRightHandedId(newRightId);
                    updatedBinding.setRightHandedKeyModifier(newRightMod);
                }
            }
            if (profileChange.leftChanged()) {
                var newLeftId = newBinding.getLeftHandedId();
                var newLeftMod = newBinding.getLeftHandedKeyModifier();
                if (newLeftId.equals(ActionBinding.ID_EMPTY) || destSupported.contains(newLeftId)) {
                    avoidCollisionInSet(destSet, destAction, profile, newLeftId, newLeftMod, true);
                    updatedBinding.setLeftHandedId(newLeftId);
                    updatedBinding.setLeftHandedKeyModifier(newLeftMod);
                }
            }

            destAction.setBinding(profile, updatedBinding);
        }
    }

    private void avoidCollisionInSet(@NotNull VRActionSet destSet,
                                     @NotNull VRAction destAction,
                                     @NotNull VRInteractionProfileType profile,
                                     @NotNull VRActionIdentifier newId,
                                     @NotNull ActionKeyModifierType newMod,
                                     boolean leftHanded) {
        if (newId.equals(ActionBinding.ID_EMPTY)) return;
        if (newId.getValue().startsWith("vec2")) return;
        for (var action : destSet.getActions()) {
            if (action == destAction) continue;
            var b = action.getBindingOrEmpty(profile);
            if (b.getActionId(leftHanded).equals(newId)
                    && b.getActionKeyModifier(leftHanded) == newMod) {
                var fixed = new ActionBinding(b);
                fixed.setActionId(ActionBinding.ID_EMPTY, leftHanded);
                fixed.setActionKeyModifier(ActionKeyModifierType.OFF, leftHanded);
                action.setBinding(profile, fixed);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        destinationList.mouseScrolled(mouseX, mouseY, delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}