package me.phoenixra.visor.api.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.common.addon.ElementPriority;
import me.phoenixra.visor.api.common.addon.PrioritySupporter;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.VisorElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

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

    public abstract void onStart();

    public abstract void onExit();


    public abstract void tick();

    public abstract void render(PoseStack poseStack, float partialTicks);


    public abstract boolean canActivate();


    public boolean isEnabledAndCanActivate() {
        return enabled && canActivate();
    }

    @Override
    public @NotNull ElementPriority getPriority() {
        return ElementPriority.NORMAL;
    }
}
