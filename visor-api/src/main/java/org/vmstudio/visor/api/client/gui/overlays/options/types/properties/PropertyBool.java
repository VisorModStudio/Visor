package org.vmstudio.visor.api.client.gui.overlays.options.types.properties;

import me.phoenixra.atumconfig.api.config.Config;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class PropertyBool extends Property<Boolean> {
    protected final WidgetInfoButtonImaged widgetInfo;
    protected final Component trueLabel;
    protected final Component falseLabel;

    public PropertyBool(@NotNull String key,
                        @NotNull Boolean defaultValue,
                        @NotNull Component trueLabel,
                        @NotNull Component falseLabel,
                        @NotNull WidgetInfoButtonImaged widgetInfo) {
        super(key, defaultValue);
        this.widgetInfo = new WidgetInfoButtonImaged(widgetInfo);
        this.trueLabel  = trueLabel;
        this.falseLabel = falseLabel;
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
    public ButtonImaged createWidget() {
        WidgetInfoButtonImaged widgetInfo
                = new WidgetInfoButtonImaged(this.widgetInfo);
        widgetInfo.setInactiveOnSelected(false);

        var button = new ButtonImaged(widgetInfo, it -> {
            boolean newVal = !getValue();
            setValue(newVal);
            onValueChanged();
            it.setSelected(newVal);
            it.setMessage(newVal ? trueLabel : falseLabel);
        });

        boolean v = getValue();
        button.setSelected(v);
        button.setMessage(v ? trueLabel : falseLabel);
        return button;
    }
}
