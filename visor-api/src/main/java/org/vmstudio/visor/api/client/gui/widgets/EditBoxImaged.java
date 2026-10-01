package org.vmstudio.visor.api.client.gui.widgets;

import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
//? if >=26.1 {
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
//?}


public class EditBoxImaged extends EditBox {
    private final GuiTexture texture;

    public EditBoxImaged(@NotNull WidgetInfoEditBox widgetInfo) {
        super(widgetInfo.getTextFont(),
                widgetInfo.getX(),
                widgetInfo.getY(),
                widgetInfo.getWidth(),
                widgetInfo.getHeight(),
                Component.empty()
        );
        this.texture = widgetInfo.getTexture();
        setTextColor(widgetInfo.getTextColor().asInt());
        // 1.21.9+ setHint dereferences its argument
        if(widgetInfo.getHint() != null) {
            setHint(widgetInfo.getHint());
        }
        setMaxLength(widgetInfo.getTextMaxLength());

        setFilter(widgetInfo.getFilter());

        setTooltip(widgetInfo.getTooltip());

        setBordered(true);
    }

    @Override
    //? if >=26.1 {
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
    //?} else {
    /*public void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
    *///?}
        if(texture != null) {
            texture.blit(
                    guiGraphics,
                    getX(), getY(),
                    getWidth(), getHeight()
            );
        }

        // draw text, cursor, selection
        //? if >=26.1 {
        super.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTick);
        //?} else {
        /*super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        *///?}
    }


    //---------
    //? if >=26.1 {
    private Predicate<String> filter = Objects::nonNull;

    public void setFilter(@NotNull Predicate<String> filter) {
        this.filter = filter;
    }

    @Override
    public void setValue(String value) {
        if (filter == null || filter.test(value)) {
            super.setValue(value);
        }
    }

    @Override
    public void insertText(String input) {
        filtered(() -> super.insertText(input));
    }

    @Override
    public void deleteCharsToPos(int pos) {
        filtered(() -> super.deleteCharsToPos(pos));
    }

    private void filtered(Runnable edit) {
        String value = getValue();
        int cursor = getCursorPosition();
        int highlight = this.highlightPos;
        Consumer<String> responder = this.responder;
        boolean[] edited = {false};
        this.responder = text -> edited[0] = true;
        try {
            edit.run();
            if (edited[0] && !filter.test(getValue())) {
                super.setValue(value);
                setCursorPosition(cursor);
                setHighlightPos(highlight);
                edited[0] = false;
            }
        } finally {
            this.responder = responder;
        }
        if (edited[0] && responder != null) {
            responder.accept(getValue());
        }
    }
    //?}



    //---------
    //silly way to make no border drawing, but have the small padding for text

    @Override
    public boolean isBordered() {
        return false;
    }

    public int getInnerWidth() {
        return this.width - 8;
    }
}
