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
public class WidgetInfoEditBox extends WidgetInfoButton{

    @Accessors(chain = true)
    private Component hint;

    public WidgetInfoEditBox(@NotNull GuiTexture texture,
                             int x, int y, int width, int height) {
        super(texture, x, y, width, height);
    }
    public WidgetInfoEditBox(@NotNull WidgetInfoEditBox copyFrom,
                             int x, int y, int width, int height) {
        super(copyFrom, x, y, width, height);
        hint = copyFrom.hint;
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
