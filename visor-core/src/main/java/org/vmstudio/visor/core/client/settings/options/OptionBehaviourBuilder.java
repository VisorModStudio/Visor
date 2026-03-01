package org.vmstudio.visor.core.client.settings.options;

import lombok.Setter;
import lombok.experimental.Accessors;
import me.phoenixra.atumconfig.api.tuples.PairRecord;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4i;

import java.util.function.Function;

public class OptionBehaviourBuilder {

    private final Function<Vector4i, AbstractWidget> widgetSupplier;

    @Setter @Accessors(chain = true)
    private Function<Object, Object> onNextValue = (old)-> null;
    @Setter @Accessors(chain = true)
    private Runnable onChanged = ()->{};
    @Setter @Accessors(chain = true)
    private Function<PairRecord<String, Object>, String> onUpdateName = (entry)->null;

    /**
     *
     * @param widgetSupplier  [Vector4 - x = posX; y = posY; z = width; w = height]
     */
    public OptionBehaviourBuilder(@NotNull Function<Vector4i, AbstractWidget> widgetSupplier) {
        this.widgetSupplier = widgetSupplier;
    }


    public @NotNull OptionBehaviour build(){
        return new OptionBehaviour() {
            @Override
            public Object nextValue(Object old) {
                return onNextValue.apply(old);
            }

            @Override
            public void onChanged() {
                onChanged.run();
            }

            @Override
            public String getDisplayString(String prefix, Object value) {
                return onUpdateName.apply(new PairRecord<>(prefix,value));
            }

            @Override
            public AbstractWidget getWidget(int x, int y, int width, int height) {
                return widgetSupplier.apply(new Vector4i(x,y,width,height));
            }
        };
    }
}
