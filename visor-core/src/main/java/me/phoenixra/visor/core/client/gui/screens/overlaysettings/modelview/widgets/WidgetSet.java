package me.phoenixra.visor.core.client.gui.screens.overlaysettings.modelview.widgets;

import me.phoenixra.visor.core.client.gui.screens.overlaysettings.OptionsScreenModelView;
import me.phoenixra.visor.core.client.gui.screens.overlaysettings.modelview.ModificationType;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.List;

public abstract class WidgetSet {

    protected final ModificationType type;

    protected final OptionsScreenModelView owner;

    public WidgetSet(ModificationType type,
                     OptionsScreenModelView owner){
        this.type = type;
        this.owner = owner;
    }

    public abstract List<AbstractWidget> getWidgets();
    public abstract List<AbstractWidget> initWidgets(int edgeX, int edgeY,
                                                     int edgeWidth, int edgeHeight);

    public abstract void onTick();
    public abstract void onRender();
}
