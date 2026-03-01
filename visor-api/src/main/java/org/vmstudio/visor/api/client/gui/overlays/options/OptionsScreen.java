package org.vmstudio.visor.api.client.gui.overlays.options;

import lombok.Getter;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class OptionsScreen<T extends OverlayOptionGroup<?>> extends Screen {
    protected final T optionsGroup;
    protected Background background;


    protected int cursorBoundsX = -1;
    protected int cursorBoundsY = -1;
    protected int cursorBoundsWidth = -1;
    protected int cursorBoundsHeight = -1;

    protected OptionsScreen(@NotNull T optionsGroup,
                            @NotNull Background background) {
        super(Component.empty());
        this.optionsGroup = optionsGroup;
        this.background = background;
    }

    protected abstract void onInit();

    protected abstract void onRender(GuiGraphics guiGraphics,
                                     int mouseX,
                                     int mouseY,
                                     float partialTick);

    @Override
    protected void init() {
        if(background != Background.EMPTY) {
            cursorBoundsX = (width - background.texture.getWidth()) / 2;
            cursorBoundsY = (height - background.texture.getHeight()) / 2;
            cursorBoundsWidth = background.texture.getWidth();
            cursorBoundsHeight = background.texture.getHeight();
        }
        clearWidgets();
        onInit();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(background != Background.EMPTY) {
            background.render(guiGraphics, cursorBoundsX, cursorBoundsY);
        }
        onRender(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }



    protected enum Background{
        FULL(OptionTextures.BACKGROUND_256x256),
        HORIZONTAL(OptionTextures.BACKGROUND_256x128),
        VERTICAL(OptionTextures.BACKGROUND_128x256),
        VERTICAL_WIDER(OptionTextures.BACKGROUND_175x256),
        EMPTY(null);

        @Getter
        private final GuiTexture texture;
        Background(GuiTexture texture){
            this.texture = texture;
        }
        public void render(GuiGraphics guiGraphics, int x, int y){
            if(texture == null) return;
            texture.blit(
                    guiGraphics,
                    x, y,
                    texture.getWidth(), texture.getHeight()
            );
        }
    }
}
