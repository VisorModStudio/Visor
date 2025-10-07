package me.phoenixra.visor.core.client.gui.overlays.builtin.settings.widgets;

import lombok.Getter;
import me.phoenixra.visor.api.client.gui.helpers.GuiHelper;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionTextures;
import me.phoenixra.visor.api.client.gui.widgets.TexturedEditBox;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import me.phoenixra.visor.api.client.gui.widgets.sets.WidgetSet;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
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

public class SetupIconWidgetSet implements WidgetSet {

    @Getter
    private final CreateOverlayWidgetSet owner;

    private final int startX;
    private final int startY;

    @Getter
    private GuiTexture icon = VisorAddon.MISSING_ICON;
    private GuiTexture preIcon =  null;


    private TexturedEditBox editorTexturePath;


    public SetupIconWidgetSet(CreateOverlayWidgetSet owner,
                              int startX, int startY){
        this.owner = owner;
        this.startX = startX;
        this.startY = startY;

    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {


        editorTexturePath = new TexturedEditBox(
                new WidgetInfoEditBox(
                        OverlayOptionTextures.DARK_GRAY_TEXTURE,
                        startX + 4, startY + 71,
                        100, 13
                ).setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setHint(Component.translatable("visor.overlay.options.main.create_overlay.type_icon_path"))
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.main.create_overlay.type_icon_path.tooltip")))
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
                Component.translatable("visor.overlay.options.main.create_overlay.load_icon").getString(),
                VROverlaySettings.TEXT_COLOR.toInt(),
                startX + 10,
                startY + 5,
                88, 8,
                true
        );
    }

    @Override
    public void onTick() {

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
