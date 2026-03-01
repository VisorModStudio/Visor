package org.vmstudio.visor.core.client.settings.options;

import net.minecraft.client.gui.components.AbstractWidget;

public abstract class OptionBehaviour {


    public Object nextValue(Object old) {
        return null;
    }

    public void onChanged() {
    }

    public String getDisplayString(String prefix, Object value) {
        return null;
    }

    public abstract AbstractWidget getWidget(int x, int y,
                                             int width, int height);
}
