package org.vmstudio.visor.core.client.gui.screens.settings.categories.controls;

import org.vmstudio.visor.api.client.input.action.framework.VRActionKeyCustom;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McGuiUtils;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRSettingsCreateCustomAction extends VROptionsSet {

    private static final String ID_PREFIX = "custom";
    private static final int OFFSET_Y = 15;
    private static final int NAME_MAX_LENGTH = 30;

    private VRSettingsActions parent;

    private boolean useMcKey;
    private String nameText = "";
    private String keyText = "";
    private @Nullable String mcKey;
    private @Nullable String suggestedName;

    private ButtonImaged keyTypeButton;
    private ButtonImaged mcKeyTypeButton;

    private EditBoxImaged actionNameEdit;
    private EditBoxImaged actionKeyEdit;
    private ButtonImaged mcKeyButton;

    private ButtonImaged createButton;

    private boolean canCreate;
    public VRSettingsCreateCustomAction(@NotNull VRSettingsActions parent,
                                     @NotNull Runnable onWidgetsChanged) {
        super(parent.getScreen(), parent, onWidgetsChanged);
        this.parent = parent;
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

        keyTypeButton = createTypeButton(
                false,
                scaleHelper.scaledX(60),
                "visor.action.options.add_custom_action.type.keyboard"
        );
        mcKeyTypeButton = createTypeButton(
                true,
                scaleHelper.scaledX(130),
                "visor.action.options.add_custom_action.type.minecraft"
        );

        actionNameEdit = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(scaleHelper.scaledX(56+36), scaleHelper.scaledY(46+OFFSET_Y))
                        .size(scaleHelper.scaledSize(72), scaleHelper.scaledSize(10))
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTextMaxLength(NAME_MAX_LENGTH)
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setTooltip(Tooltip.create(Component.translatable("visor.action.options.add_custom_action.type_name.tooltip")))
                        .setHint(Component.translatable("visor.action.options.add_custom_action.type_name"))
        );
        actionNameEdit.setValue(nameText);
        actionNameEdit.setResponder(it -> nameText = it);

        actionKeyEdit = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(scaleHelper.scaledX(56+36+18), scaleHelper.scaledY(67+OFFSET_Y))
                        .size(scaleHelper.scaledSize(36), scaleHelper.scaledSize(10))
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTextMaxLength(1)
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setTooltip(Tooltip.create(Component.translatable("visor.action.options.add_custom_action.type_key.tooltip")))
                        .setHint(Component.translatable("visor.action.options.add_custom_action.type_key"))
        );
        actionKeyEdit.setValue(keyText);
        actionKeyEdit.setResponder(it -> keyText = it);

        mcKeyButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(78), scaleHelper.scaledY(67+OFFSET_Y))
                        .size(scaleHelper.scaledSize(100), scaleHelper.scaledSize(10))
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setDynamicTextScale(true)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setText(mcKey != null
                                ? Component.translatable(mcKey)
                                : Component.translatable("visor.action.options.add_custom_action.select_keybind")),
                (it)->{
                    switchOptions(new VRSettingsSelectKeyMapping(this, onWidgetsChanged));
                }
        );

        createButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(scaleHelper.scaledX(96),scaleHelper.scaledY(136))
                        .size(scaleHelper.scaledSize(64), scaleHelper.scaledSize(10))
                        .setTexture(OptionTextures.LIGHT_GRAY_TEXTURE_2)
                        .setTextureInactive(OptionTextures.GRAY_TEXTURE)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                        .setTextScale(VRClientSettings.getSettingsTextScale())
                        .setText(Component.translatable("visor.action.options.add_custom_action.create")),
                (it)->{
                    createPressed();
                }
        );
        return getWidgets();
    }

    private ButtonImaged createTypeButton(boolean mcKeyType, int x, @NotNull String langKey){
        var scaleHelper = getScreen().getScaleHelper();
        var button = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(x, scaleHelper.scaledY(29+OFFSET_Y))
                        .size(scaleHelper.scaledSize(66), scaleHelper.scaledSize(10))
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setInactiveOnSelected(false)
                        .setDynamicTextScale(true)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                        .setTooltip(Tooltip.create(Component.translatable(langKey + ".tooltip")))
                        .setText(Component.translatable(langKey)),
                (it)->{
                    if(useMcKey == mcKeyType) return;
                    useMcKey = mcKeyType;
                    reinit();
                }
        );
        button.setSelected(useMcKey == mcKeyType);
        return button;
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        list.add((T) keyTypeButton);
        list.add((T) mcKeyTypeButton);
        list.add((T) actionNameEdit);
        if(useMcKey){
            list.add((T) mcKeyButton);
        }else{
            list.add((T) actionKeyEdit);
        }
        list.add((T) createButton);
        return list;
    }

    @Override
    public void onTick() {
        super.onTick();
        McGuiUtils.tickEditBox(actionNameEdit);
        if(!useMcKey) {
            McGuiUtils.tickEditBox(actionKeyEdit);
        }

        checkCreateRequirements();
    }

    @Override
    public void onPostRender(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        var scaleHelper = getScreen().getScaleHelper();

        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                Component.translatable("visor.action.options.add_custom_action").getString(),
                AtumColor.WHITE.asInt(),
                scaleHelper.scaledX(60), scaleHelper.scaledY(31),
                scaleHelper.scaledSize(136), scaleHelper.scaledSize(7),
                true
        );
        if(createButton.active) {
            GuiHelper.renderScalableText(
                    guiGraphics,
                    MC.font,
                    Component.translatable("visor.action.options.add_custom_action.id", generateId()).getString(),
                    VROverlaySettings.ID_TEXT_COLOR.asInt(),
                    scaleHelper.scaledX(56 + 36 + 4), scaleHelper.scaledY(58 + OFFSET_Y),
                    scaleHelper.scaledSize(64), scaleHelper.scaledSize(5),
                    false
            );
        }
    }

    void selectKeyMapping(@NotNull String mcKey){
        String label = Component.translatable(mcKey).getString();
        if(label.length() > NAME_MAX_LENGTH){
            label = label.substring(0, NAME_MAX_LENGTH);
        }
        if(nameText.isEmpty() || nameText.equals(suggestedName)){
            nameText = label;
        }
        suggestedName = label;
        this.mcKey = mcKey;
    }

    private void createPressed(){
        checkCreateRequirements();
        if(!canCreate){
            return;
        }

        var id = generateId();
        VRActionKeyCustom customAction;
        if(useMcKey){
            String name = nameText.equals(suggestedName) ? mcKey : nameText;
            customAction = new VRActionKeyCustom(id, parent.getActionSet(), mcKey, name);
        }else{
            customAction = new VRActionKeyCustom(id, parent.getActionSet(), keyText.charAt(0), nameText);
        }
        parent.getActionSet().addCustomAction(
                customAction
        );
        parent.getActionSet().saveCustomActions();

        parent.addedCustomAction(customAction);
        parent.getScreen().switchOptions(parent);

    }


    private String generateId(){
        return ComponentIds.generateId(
                nameText,
                ID_PREFIX,
                id -> parent.getActionSet().getAction(id) != null
        );
    }

    private void checkCreateRequirements(){
        canCreate = true;

        if(nameText.length() < 3){
            canCreate = false;
            if(!nameText.isEmpty()) {
                actionNameEdit.setTextColor(AtumColor.RED.asInt());
            }
        }else{
            actionNameEdit.setTextColor(AtumColor.WHITE.asInt());
        }
        if(useMcKey ? mcKey == null : keyText.isEmpty()){
            canCreate = false;
        }

        createButton.active = canCreate;
    }

}
