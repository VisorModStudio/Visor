package me.phoenixra.visor.api.client.gui.overlay.options.types.properties;

import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.visor.api.client.gui.widgets.ImageButton;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class PropertyBool extends Property<Boolean> {
    protected final WidgetInfoButton widgetInfo;
    protected final Component trueLabel;
    protected final Component falseLabel;

    public PropertyBool(@NotNull String key,
                        @NotNull Boolean defaultValue,
                        @NotNull WidgetInfoButton widgetInfo) {
        super(key, defaultValue);
        this.widgetInfo = new WidgetInfoButton(widgetInfo, 0,0,1,1);
        this.trueLabel  = Component.translatable("options.on");
        this.falseLabel = Component.translatable("options.off");
    }

    @Override
    public void onLoad(@NotNull Config config) {
        setValue(config.getBoolOrDefault(key, defaultValue));
    }

    @Override
    public void onSave(@NotNull Config config) {
        config.set(key, getValue());
    }

    @Override
    public AbstractWidget createWidget(int x, int y, int width, int height) {
        WidgetInfoButton widgetInfo = new WidgetInfoButton(
                this.widgetInfo,
                x,y,width,height
        );

        var button = new ImageButton(widgetInfo, imgBtn -> {
            boolean newVal = !getValue();
            setValue(newVal);
            imgBtn.setSelected(newVal);
            imgBtn.setMessage(newVal ? trueLabel : falseLabel);
        });

        boolean v = getValue();
        button.setSelected(v);
        button.setMessage(v ? trueLabel : falseLabel);
        return button;
    }
}
