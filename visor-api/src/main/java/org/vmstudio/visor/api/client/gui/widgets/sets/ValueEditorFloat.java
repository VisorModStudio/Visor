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

public class ValueEditorFloat implements WidgetSet{
    private int x, y, width, height;

    private WidgetInfoEditBox editBoxInfo;
    private WidgetInfoValueDrag leftArrowInfo;
    private WidgetInfoValueDrag rightArrowInfo;


    private EditBoxImaged editBox;
    private ValueDragWidget leftArrow;
    private ValueDragWidget rightArrow;


    @Getter
    private float value;

    private float minValue, maxValue;

    private Consumer<Float> responder;

    public ValueEditorFloat(@NotNull Builder builder){
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
                        v -> setValue((float) v, true)
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
                        v -> setValue((float) v, true)
                ));;


        this.value = builder.initialValue;
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;

        this.responder = builder.responder;
    }

    public void setValue(float value, boolean updateEditBox) {
        this.value = Mth.clamp(value, minValue, maxValue);
        if(updateEditBox && editBox != null){
            editBox.setValue(formatFloat(this.value));
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
        editBox.setValue(formatFloat(getValue()));
        editBox.setFilter(s -> {
            if (s.isEmpty()) return true;
            if (minValue < 0f && (s.equals("-") || s.equals("-."))) return true;
            if (s.equals(".")) return true;

            try {
                float v = Float.parseFloat(s);
                return Float.isFinite(v) && v >= minValue && v <= maxValue;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        editBox.setResponder(s -> {
            try {
                float v = Float.parseFloat(s);
                if (Float.isFinite(v) && v >= minValue && v <= maxValue) {
                    setValue(v, false);
                }
            } catch (NumberFormatException ignored) {

            }
        });
        int intPartWidth = Math.max(
                Integer.toString((int)Math.floor(Math.abs(minValue))).length(),
                Integer.toString((int)Math.floor(Math.abs(maxValue))).length()
        );
        int maxLen = intPartWidth + 1 /* '.' */ + 8 /* decimals */ + (minValue < 0 ? 1 : 0);
        editBox.setMaxLength(Math.max(maxLen, 6));

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

    private static String formatFloat(float v) {
        String s = String.format(java.util.Locale.ROOT, "%.6f", v);
        int dot = s.indexOf('.');
        if (dot >= 0) {
            int end = s.length();
            while (end > dot + 1 && s.charAt(end - 1) == '0') end--;
            if (end == dot + 1) end = dot;
            s = s.substring(0, end);
        }
        return s;
    }

    public static class Builder {
        private int x, y, width, height;
        @Setter @Accessors(chain = true)
        private int gap = 4;

        private WidgetInfoEditBox widgetInfoEditBox;
        private WidgetInfoValueDrag widgetInfoLeftArrow;
        private WidgetInfoValueDrag widgetInfoRightArrow;


        private float initialValue;

        private float minValue = -Float.MAX_VALUE, maxValue = Float.MAX_VALUE;

        @Setter @Accessors(chain = true)
        private Consumer<Float> responder;

        public Builder(float initialValue, int x, int y, int width, int height) {
            this.initialValue = initialValue;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Builder range(float min, float max){
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
        public ValueEditorFloat build() {
            return new ValueEditorFloat(this);
        }
    }
}
