package me.phoenixra.visor.core.client.settings.options;

import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

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
