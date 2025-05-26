package me.phoenixra.visor.api.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.VisorElement;
import org.jetbrains.annotations.NotNull;

public abstract class VRDecorator implements VisorElement, Comparable<VRDecorator>  {
    @Getter
    private final VisorAddon owner;
    @Getter
    private final String id;


    @Getter @Setter
    private boolean enabled;

    public VRDecorator(@NotNull VisorAddon owner,
                       @NotNull String id){
        this.owner = owner;
        this.id = id;
    }

    public abstract void onStart();

    public abstract void onExit();


    public abstract void tick();

    public abstract void render(PoseStack poseStack, float partialTicks);


    public abstract boolean isDisplayable();


    public abstract int getPriority();

    @Override
    public int compareTo(@NotNull VRDecorator o) {
        return getPriority() - o.getPriority();
    }
}
