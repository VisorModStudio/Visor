package org.vmstudio.visor.api.client.gui.widgets.sets;

import org.jetbrains.annotations.NotNull;

public abstract class DynamicWidgetSet implements WidgetSet{
    protected final Runnable onWidgetsChanged;
    public DynamicWidgetSet(@NotNull Runnable onWidgetsChanged){
        this.onWidgetsChanged = onWidgetsChanged;
    }
    protected void widgetsChanged(){
        onWidgetsChanged.run();
    }
}
