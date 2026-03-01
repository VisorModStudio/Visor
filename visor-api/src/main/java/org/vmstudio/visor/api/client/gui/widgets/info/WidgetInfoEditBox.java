package org.vmstudio.visor.api.client.gui.widgets.info;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

@Setter
@Getter
public class WidgetInfoEditBox extends WidgetInfoImage {

    @Accessors(chain = true)
    private Component hint;

    @Accessors(chain = true)
    private Font textFont = Minecraft.getInstance().font;
    @Accessors(chain = true)
    private AtumColor textColor = AtumColor.WHITE;
    @Accessors(chain = true)
    private int textMaxLength = 35;

    @Accessors(chain = true)
    private Predicate<String> filter = Objects::nonNull;


    @Accessors(chain = true)
    private Tooltip tooltip;


    public WidgetInfoEditBox(@NotNull WidgetInfoEditBox copyFrom) {
        super(copyFrom);
        hint = copyFrom.hint;
        textFont = copyFrom.textFont;
        textColor = copyFrom.textColor;
        textMaxLength = copyFrom.textMaxLength;
        filter = copyFrom.filter;
        tooltip = copyFrom.tooltip;
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


}
