package org.vmstudio.visor.api.client.gui.widgets.sets;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.ValueDragWidget;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoValueDrag;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ValueEditorInt implements WidgetSet{
    private int x, y, width, height;

    private WidgetInfoEditBox editBoxInfo;
    private WidgetInfoValueDrag leftArrowInfo;
    private WidgetInfoValueDrag rightArrowInfo;


    @Getter
    private EditBoxImaged editBox;
    @Getter
    private ValueDragWidget leftArrow;
    @Getter
    private ValueDragWidget rightArrow;


    @Getter
    private int value;

    private int minValue, maxValue;

    private Consumer<Integer> responder;

    private ValueEditorInt(@NotNull Builder builder){
        x = builder.x;
        y = builder.y;
        width = builder.width;
        height = builder.height;
        editBoxInfo = builder.widgetInfoEditBox;
        leftArrowInfo = builder.widgetInfoLeftArrow;
        rightArrowInfo = builder.widgetInfoRightArrow;

        leftArrowInfo
                .pos(x,y)
                .size(height, height)
                .setDirection(WidgetInfoValueDrag.Direction.LEFT)
                .setAdapter(WidgetInfoValueDrag.NumericAdapter.of(
                        () -> value,
                        v -> setValue((int) v, true)
                ));
        editBoxInfo
                .pos(x + height + builder.gap, y)
                .size(width - (height + builder.gap)*2, height);
        rightArrowInfo
                .pos(editBoxInfo.getX()
                                + editBoxInfo.getWidth()
                                + builder.gap,
                        y
                ).size(height, height)
                .setDirection(WidgetInfoValueDrag.Direction.RIGHT)
                .setAdapter(WidgetInfoValueDrag.NumericAdapter.of(
                        () -> value,
                        v -> setValue((int) v, true)
                ));;


        this.value = builder.initialValue;
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.responder = builder.responder;
    }

    public void setValue(int value, boolean updateEditBox) {
        this.value = Mth.clamp(value, minValue, maxValue);
        if(updateEditBox && editBox != null){
            editBox.setValue(Integer.toString(this.value));
        }
        if(responder != null){
            responder.accept(this.value);
        }
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {
        editBox = new EditBoxImaged(editBoxInfo);
        editBox.setValue(Integer.toString((getValue())));

        editBox.setFilter(s -> {
            if (s.isEmpty() || s.equals("-")) return true;
            try {
                int v = Integer.parseInt(s);
                return v >= minValue && v <= maxValue;
            } catch (NumberFormatException e) {
                return false;
            }
        });

        editBox.setResponder(s -> {
            if (s.isEmpty() || s.equals("-")) return;
            try {
                setValue(Integer.parseInt(s), false);
            } catch (NumberFormatException ignored) { }
        });

        int maxDigits = Math.max(
                Integer.toString(Math.abs(minValue)).length(),
                Integer.toString(Math.abs(maxValue)).length()
        ) + (minValue < 0 ? 1 : 0);
        editBox.setMaxLength(maxDigits);

        editBox.moveCursorToStart(false);


        leftArrow = new ValueDragWidget(leftArrowInfo);
        rightArrow = new ValueDragWidget(rightArrowInfo);

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> widgets = new ArrayList<>();
        widgets.add((T) editBox);
        widgets.add((T) leftArrow);
        widgets.add((T) rightArrow);
        return widgets;
    }

    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void onTick() {
    }


    public static class Builder {
        private int x, y, width, height;
        @Setter @Accessors(chain = true)
        private int gap = 2;

        private WidgetInfoEditBox widgetInfoEditBox;
        private WidgetInfoValueDrag widgetInfoLeftArrow;
        private WidgetInfoValueDrag widgetInfoRightArrow;


        private int initialValue;

        private int minValue = Integer.MIN_VALUE, maxValue = Integer.MAX_VALUE;

        @Setter @Accessors(chain = true)
        private Consumer<Integer> responder;

        public Builder(int initialValue,
                       int x, int y,
                       int width, int height) {
            this.initialValue = initialValue;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Builder range(int min, int max){
            this.minValue = min;
            this.maxValue = max;
            return this;
        }

        /**
         * Provide widget info for edit box. <br>
         * Don't specify blockPos and size, it will be overwritten
         */
        public Builder editBox(WidgetInfoEditBox widgetInfo){
            widgetInfoEditBox = widgetInfo;
            return this;
        }

        /**
         * Provide widget info for left arrow. <br>
         * Don't specify blockPos, size, direction, adapter. They will be overwritten
         */
        public Builder leftArrow(WidgetInfoValueDrag widgetInfo){
            widgetInfoLeftArrow = widgetInfo;
            return this;
        }

        /**
         * Provide widget info for right arrow. <br>
         * Don't specify blockPos, size, direction, adapter. They will be overwritten
         */
        public Builder rightArrow(WidgetInfoValueDrag widgetInfo){
            widgetInfoRightArrow = widgetInfo;
            return this;
        }

        /**
         * Builds the configured widget set
         */
        public ValueEditorInt build() {
            return new ValueEditorInt(this);
        }
    }
}
