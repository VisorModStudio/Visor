package me.phoenixra.visor.api.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.element.PrioritySupporter;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.element.VisorElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class VRDecorator implements VisorElement, PrioritySupporter {
    @Getter
    private final VisorAddon owner;
    @Getter
    private final String id;


    @Getter @Setter
    private boolean enabled = true;


    public VRDecorator(@NotNull VisorAddon owner,
                       @NotNull String id){
        this.owner = owner;
        this.id = id;
    }

    public void init(){

    }

    public void clear(){

    }


    public abstract void tick();

    public abstract void render(PoseStack poseStack, float partialTicks);


    public abstract boolean canActivate();

    /**
     * Supported game effects(non-global)
     *
     * @return list of non-global game effect ids
     */
    public abstract List<String> gameEffects();

    /**
     * Supported hand effects(non-global)
     *
     * @return list of non-global hand effect ids
     */
    public abstract List<String> handEffects();


    public boolean isEnabledAndCanActivate() {
        return enabled && canActivate();
    }

    @Override
    public @NotNull ElementPriority getPriority() {
        return ElementPriority.NORMAL;
    }
}
