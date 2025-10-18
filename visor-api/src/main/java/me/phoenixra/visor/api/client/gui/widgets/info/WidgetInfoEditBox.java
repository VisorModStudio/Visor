package me.phoenixra.visor.api.client.gui.widgets.info;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public class WidgetInfoEditBox extends WidgetInfoButtonImaged {

    @Accessors(chain = true)
    private Component hint;


    public WidgetInfoEditBox(@NotNull WidgetInfoEditBox copyFrom) {
        super(copyFrom);
        hint = copyFrom.hint;
    }

    public WidgetInfoEditBox() {

    }

    @Override
    public WidgetInfoEditBox pos(int x, int y) {
        return (WidgetInfoEditBox) super.pos(x, y);
    }

    @Override
    public WidgetInfoEditBox size(int width, int height) {
        return (WidgetInfoEditBox) super.size(width, height);
    }

    @Override
    public WidgetInfoEditBox setTexture(GuiTexture texture) {
        return (WidgetInfoEditBox) super.setTexture(texture);
    }

    @Override
    public WidgetInfoEditBox setText(Component text) {
        return (WidgetInfoEditBox) super.setText(text);
    }

    @Override
    public WidgetInfoEditBox setInactiveOnSelected(boolean inactiveOnSelected) {
        return (WidgetInfoEditBox) super.setInactiveOnSelected(inactiveOnSelected);
    }

    @Override
    public WidgetInfoEditBox setTextColor(AtumColor textColor) {
        return (WidgetInfoEditBox) super.setTextColor(textColor);
    }

    @Override
    public WidgetInfoEditBox setTextFont(Font textFont) {
        return (WidgetInfoEditBox) super.setTextFont(textFont);
    }

    @Override
    public WidgetInfoEditBox setTextureHovered(GuiTexture textureHovered) {
        return (WidgetInfoEditBox) super.setTextureHovered(textureHovered);
    }

    @Override
    public WidgetInfoEditBox setTextureHoveredSelected(GuiTexture textureHoveredSelected) {
        return (WidgetInfoEditBox) super.setTextureHoveredSelected(textureHoveredSelected);
    }

    @Override
    public WidgetInfoEditBox setTextureSelected(GuiTexture textureSelected) {
        return (WidgetInfoEditBox) super.setTextureSelected(textureSelected);
    }

    @Override
    public WidgetInfoEditBox setTooltip(Tooltip tooltip) {
        return (WidgetInfoEditBox) super.setTooltip(tooltip);
    }
}
