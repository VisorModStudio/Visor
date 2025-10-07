package me.phoenixra.visor.api.client.gui.widgets;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.tuples.PairRecord;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class DiscreteSliderWidget <T> extends AbstractSliderButton {

    private final List<T> entries;
    private final Consumer<PairRecord<DiscreteSliderWidget<T>, T>> onChange;


    @Getter @Setter
    private Component message;


    @Getter
    private int index;

    /**
     * @param x           left
     * @param y           top
     * @param width       control width
     * @param height      control height (20 is the vanilla height)
     * @param entries     non-empty list of values to step through
     * @param initialIndex  starting index (clamped)
     * @param onChange    called when selection changes; may be null
     */
    public DiscreteSliderWidget(
            int x, int y, int width, int height,
            List<T> entries,
            int initialIndex,
            Consumer<PairRecord<DiscreteSliderWidget<T>, T>> onChange
    ) {
        super(
                x, y, width, height,
                Component.empty(),
                normalizeIndex(
                        Math.max(0, Math.min(entries.size() - 1, initialIndex)),
                        entries.size()
                )
        );
        this.entries = List.copyOf(entries);
        this.onChange = onChange != null ? onChange : t -> {};

        this.index = clampIndex(initialIndex);

        snapToIndex();
        updateMessage();
    }


    @Override
    protected void updateMessage() {

        setMessage(message);
    }

    @Override
    protected void applyValue() {
        int newIndex = valueToNearestIndex(this.value);
        if (newIndex != this.index) {
            this.index = newIndex;
            this.onChange.accept(new PairRecord<>(this, entries.get(this.index)));
        }

        snapToIndex();
        updateMessage();
    }


    public T getSelected() {
        return entries.get(index);
    }

    /** Sets the selection and updates the knob+label, firing onChange. */
    public void setIndex(int newIndex) {
        if (entries.isEmpty()) return;
        int clamped = clampIndex(newIndex);
        if (clamped != this.index) {
            this.index = clamped;
            snapToIndex();
            updateMessage();
            this.onChange.accept(new PairRecord<>(this, entries.get(this.index)));
        }
    }

    /** Finds the first equal element and selects it. No-op if not found. */
    public void setSelected(T value) {
        int idx = entries.indexOf(value);
        if (idx >= 0) setIndex(idx);
    }



    private int clampIndex(int idx) {
        return Mth.clamp(idx, 0, entries.size() - 1);
    }

    private void snapToIndex() {
        this.value = normalizeIndex(this.index, entries.size());
    }

    private static double normalizeIndex(int idx, int size) {
        if (size <= 1) return 0.0D;
        return (double) idx / (double) (size - 1);
    }

    private int valueToNearestIndex(double v) {
        if (entries.size() <= 1) return 0;
        double clamped = Mth.clamp(v, 0.0D, 1.0D);
        int nearest = (int) Math.round(clamped * (entries.size() - 1));
        return Mth.clamp(nearest, 0, entries.size() - 1);
    }
}
