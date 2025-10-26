package me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.widgets;

import me.phoenixra.visor.core.client.gui.screens.overlayoptions.OptionsScreenPose;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.ModificationType;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.List;

public abstract class PoseWidgetSet {

    protected final ModificationType type;

    protected final OptionsScreenPose owner;

    public PoseWidgetSet(ModificationType type,
                         OptionsScreenPose owner){
        this.type = type;
        this.owner = owner;
    }

    public abstract List<AbstractWidget> getWidgets();
    public abstract List<AbstractWidget> initWidgets(int edgeX, int edgeY,
                                                     int edgeWidth, int edgeHeight);

    public abstract void onTick();
    public abstract void onRender();
}
