package org.vmstudio.visor.core.client.gui.screens.settings.categories.controls;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.input.action.VisorActionKey;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRSettingsCreateKeyAction extends VROptionsSet {

    private VRSettingsActions parent;

    private EditBoxImaged actionIdEdit;
    private EditBoxImaged actionNameEdit;
    private EditBoxImaged actionKeyEdit;

    private ButtonImaged createButton;

    private boolean canCreate;
    public VRSettingsCreateKeyAction(@NotNull VRSettingsActions parent,
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

        int offsetY = 15;
        actionIdEdit = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(scaleHelper.scaledX(56+36), scaleHelper.scaledY(46+offsetY))
                        .size(scaleHelper.scaledSize(72), scaleHelper.scaledSize(10))
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTextMaxLength(20)
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setTooltip(Tooltip.create(Component.translatable("visor.action.options.add_key_action.type_id.tooltip")))
                        .setHint(Component.translatable("visor.action.options.add_key_action.type_id"))
        );

        actionNameEdit = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(scaleHelper.scaledX(56+36), scaleHelper.scaledY(60+offsetY))
                        .size(scaleHelper.scaledSize(72), scaleHelper.scaledSize(10))
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTextMaxLength(30)
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setTooltip(Tooltip.create(Component.translatable("visor.action.options.add_key_action.type_name.tooltip")))
                        .setHint(Component.translatable("visor.action.options.add_key_action.type_name"))
        );

        actionKeyEdit = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(scaleHelper.scaledX(56+36+18), scaleHelper.scaledY(74+offsetY))
                        .size(scaleHelper.scaledSize(36), scaleHelper.scaledSize(10))
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTextMaxLength(1)
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setTooltip(Tooltip.create(Component.translatable("visor.action.options.add_key_action.type_key.tooltip")))
                        .setHint(Component.translatable("visor.action.options.add_key_action.type_key"))
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
                        .setText(Component.translatable("visor.action.options.add_key_action.create")),
                (it)->{
                    createPressed();
                }
        );
        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        list.add((T) actionIdEdit);
        list.add((T) actionNameEdit);
        list.add((T) actionKeyEdit);
        list.add((T) createButton);
        return list;
    }

    @Override
    public void onTick() {
        super.onTick();
        actionIdEdit.tick();
        actionNameEdit.tick();
        actionKeyEdit.tick();

        checkCreateRequirements();
    }

    @Override
    public void onPostRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        var scaleHelper = getScreen().getScaleHelper();

        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                Component.translatable("visor.action.options.add_key_action").getString(),
                AtumColor.WHITE.asInt(), 
                scaleHelper.scaledX(60), scaleHelper.scaledY(31),
                scaleHelper.scaledSize(136), scaleHelper.scaledSize(7),
                true
        );
    }

    private void createPressed(){
        checkCreateRequirements();
        if(!canCreate){
            return;
        }

        var id = actionIdEdit.getValue();
        var name = actionNameEdit.getValue();
        var key = actionKeyEdit.getValue();
        var keyAction = new VisorActionKey(
                id,
                parent.getActionSet(),
                key.charAt(0), name
        );
        parent.getActionSet().addKeyAction(
                keyAction
        );
        parent.getActionSet().saveKeyActions();

        parent.addedKeyAction(keyAction);
        parent.getScreen().switchOptions(parent);

    }


    private void checkCreateRequirements(){
        String id = actionIdEdit.getValue();
        String name = actionNameEdit.getValue();
        String key = actionKeyEdit.getValue();

        canCreate = true;

        if(id.length() < 3){
            canCreate = false;
            if(!id.isEmpty()) {
                actionIdEdit.setTextColor(AtumColor.RED.asInt());
            }
        }
        else if(parent.getActionSet().getAction(id) != null){
            canCreate = false;
            actionIdEdit.setTextColor(AtumColor.RED.asInt());
        }else if(!ComponentIds.isValid(id)){
            canCreate = false;
            actionIdEdit.setTextColor(AtumColor.RED.asInt());
        }else{
            actionIdEdit.setTextColor(AtumColor.WHITE.asInt());
        }
        if(name.length() < 3){
            canCreate = false;
            if(!name.isEmpty()) {
                actionNameEdit.setTextColor(AtumColor.RED.asInt());
            }
        }else{
            actionNameEdit.setTextColor(AtumColor.WHITE.asInt());
        }
        if(key.isEmpty()){
            canCreate = false;
        }

        createButton.active = canCreate;
    }

}
