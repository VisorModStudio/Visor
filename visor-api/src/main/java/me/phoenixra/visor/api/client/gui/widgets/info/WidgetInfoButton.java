package me.phoenixra.visor.api.client.gui.widgets.info;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

@Setter
@Getter
public class WidgetInfoButton extends WidgetInfoImage {

    @Accessors(chain = true)
    private GuiTexture textureHovered;

    @Accessors(chain = true)
    private GuiTexture textureSelected;

    @Accessors(chain = true)
    private GuiTexture textureHoveredSelected;

    @Accessors(chain = true)
    private GuiTexture textureInactive;

    @Accessors(chain = true)
    private Font textFont = Minecraft.getInstance().font;

    @Accessors(chain = true)
    private Component text = Component.empty();

    @Accessors(chain = true)
    private AtumColor textColor = AtumColor.WHITE;

    @Accessors(chain = true)
    private Vector2i textPosOffset = new Vector2i(0,0);

    @Accessors(chain = true)
    private Vector2i textScaleOffset = new Vector2i(0,0);

    @Accessors(chain = true)
    private boolean inactiveOnSelected = true;

    @Accessors(chain = true)
    private Tooltip tooltip;

    public WidgetInfoButton(@NotNull GuiTexture texture,
                            int x, int y, int width, int height) {
        this(texture, texture, x, y, width, height);
    }

    public WidgetInfoButton(@NotNull GuiTexture texture,
                            @NotNull GuiTexture textureHovered,
                            int x, int y, int width, int height) {
        super(texture, x, y, width, height);
        this.textureHovered = textureHovered;
        this.textureInactive = texture;
    }
    public WidgetInfoButton(@NotNull WidgetInfoButton copyFrom,
                            int x, int y, int width, int height) {
        super(copyFrom, x, y, width, height);
        textureHovered = copyFrom.textureHovered;
        textureSelected = copyFrom.textureSelected;
        textureHoveredSelected = copyFrom.textureHoveredSelected;
        textureInactive = copyFrom.textureInactive;
        textFont = copyFrom.textFont;
        text = copyFrom.text;
        textColor = copyFrom.textColor;
        textPosOffset = copyFrom.textPosOffset;
        textScaleOffset = copyFrom.textScaleOffset;
        inactiveOnSelected = copyFrom.inactiveOnSelected;
        tooltip = copyFrom.tooltip;
    }

    @Override
    public WidgetInfoButton setTexture(GuiTexture texture) {
        return (WidgetInfoButton) super.setTexture(texture);
    }

}
