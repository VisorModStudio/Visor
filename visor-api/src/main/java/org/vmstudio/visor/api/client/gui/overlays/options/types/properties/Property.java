package org.vmstudio.visor.api.client.gui.overlays.options.types.properties;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.Config;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class Property<T>{
    protected final String key;
    protected final T defaultValue;

    @Setter
    private Runnable responder;

    @Setter
    private T value;
    public Property(@NotNull String key, @NotNull T defaultValue){
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    protected final void onValueChanged(){
        if(responder != null) {
            responder.run();
        }
    }
    public void update(){

    }
    public void loadDefault(){
        value = defaultValue;
    }
    public abstract void onLoad(@NotNull Config config);
    public abstract void onSave(@NotNull Config config);

    public abstract AbstractWidget createWidget();
}
