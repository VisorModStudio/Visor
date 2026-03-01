package org.vmstudio.visor.api.client.gui.widgets.info;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
@Setter
@Getter
public class WidgetInfoTextBoxEditable extends WidgetInfo {

    @Accessors(chain = true)
    private GuiTexture background;

    @Accessors(chain = true)
    private GuiTexture textureScrollBar = OptionTextures.SCROLL_BAR;
    @Accessors(chain = true)
    private GuiTexture textureScrollBarActive = OptionTextures.SCROLL_BAR_ACTIVE;
    @Accessors(chain = true)
    private int scrollBarWidth = 4;

    @Accessors(chain = true)
    private Font textFont = Minecraft.getInstance().font;
    @Accessors(chain = true)
    private Component text = Component.empty();
    @Accessors(chain = true)
    private AtumColor textColor = AtumColor.WHITE;
    @Accessors(chain = true)
    private AtumColor textHintColor = AtumColor.immutable(119,119,119,255);
    @Accessors(chain = true)
    private AtumColor highlightColor = AtumColor.immutable(0,0,255,255);
    @Accessors(chain = true)
    private float textScale = 1.0f;



    @Accessors(chain = true)
    private int maxLength = 200;
    @Accessors(chain = true)
    private Component hint;


    @Setter @Accessors(chain = true)
    private Supplier<Component> tooltip;

    public WidgetInfoTextBoxEditable(@NotNull WidgetInfoTextBoxEditable copyFrom) {
        super(copyFrom);
        background = copyFrom.background;
        textureScrollBar = copyFrom.textureScrollBar;
        textureScrollBarActive = copyFrom.textureScrollBarActive;
        textFont = copyFrom.textFont;
        text = copyFrom.text;
        textColor = copyFrom.textColor;
        textHintColor = copyFrom.textHintColor;
        highlightColor = copyFrom.highlightColor;
        textScale = copyFrom.textScale;
        scrollBarWidth = copyFrom.scrollBarWidth;
        maxLength = copyFrom.maxLength;
        hint = copyFrom.hint;
        tooltip = copyFrom.tooltip;
    }

    public WidgetInfoTextBoxEditable() {

    }

    @Override
    public WidgetInfoTextBoxEditable pos(int x, int y) {
        return (WidgetInfoTextBoxEditable) super.pos(x, y);
    }

    @Override
    public WidgetInfoTextBoxEditable size(int width, int height) {
        return (WidgetInfoTextBoxEditable) super.size(width, height);
    }
}
