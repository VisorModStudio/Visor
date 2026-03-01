package org.vmstudio.visor.core.client.gui.overlays.builtin.settings.widgets.identity;

import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.gui.widgets.sets.WidgetSet;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SetupIconWidgetSet implements WidgetSet {


    private final int startX;
    private final int startY;

    @Getter
    private GuiTexture icon = VisorAddon.MISSING_ICON;
    private GuiTexture preIcon =  null;


    private EditBoxImaged editorTexturePath;

    @Setter
    private Consumer<String> responder;

    public SetupIconWidgetSet(int startX, int startY){
        this.startX = startX;
        this.startY = startY;

    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {


        editorTexturePath = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(startX + 4, startY + 71)
                        .size(100, 13)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setHint(Component.translatable("visor.overlay.options.overlays.create_overlay.type_icon_path"))
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.overlays.create_overlay.type_icon_path.tooltip")))
        );


        editorTexturePath.setResponder((it)-> tryLoadPreIcon());

        editorTexturePath.setMaxLength(80);

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        list.add((T)editorTexturePath);
        return list;
    }

    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        boolean iconUpdate = false;
        try {
            if(preIcon != null){
                preIcon.blit(
                        guiGraphics,
                        startX + 19,
                        startY + 18,
                        40, 40
                );
                preIcon.blit(
                        guiGraphics,
                        startX + 70,
                        startY + 28,
                        19, 19
                );
                icon = preIcon;
                preIcon = null;
                iconUpdate = true;
            }
        } catch (Exception e) {
            icon = VisorAddon.MISSING_ICON;
            preIcon = null;
        }
        icon.blit(
                guiGraphics,
                startX + 19,
                startY + 18,
                40, 40
        );
        icon.blit(
                guiGraphics,
                startX + 70,
                startY + 28,
                19, 19
        );


        GuiHelper.renderScalableText(
                guiGraphics,
                Minecraft.getInstance().font,
                Component.translatable("visor.overlay.options.overlays.create_overlay.load_icon").getString(),
                VROverlaySettings.TEXT_COLOR.asInt(),
                startX + 10,
                startY + 5,
                88, 8,
                true
        );

        if(iconUpdate && responder != null){
            responder.accept(editorTexturePath.getValue());
        }
    }

    @Override
    public void onTick() {
        editorTexturePath.tick();
    }

    public void setIconPath(String path){
        editorTexturePath.setValue(path);
        tryLoadPreIcon();
    }

    private void tryLoadPreIcon(){
        icon = VisorAddon.MISSING_ICON; //reset to default

        if(editorTexturePath.getValue().isEmpty()){
            return;
        }
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        try {
            String path = editorTexturePath.getValue();
            if(!path.endsWith(".png")){
                return;
            }

            var resourceLoc = new ResourceLocation(path);


            var resource = resourceManager.getResource(resourceLoc);
            if(resource.isEmpty()){
                return;
            }

            preIcon = new GuiTexture(
                    resourceLoc
            );
        }catch (Exception ignored){
        }
    }
}
