package me.phoenixra.visor.core.client.render.decoration.decorators;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRDecorator;
import me.phoenixra.visor.api.common.addon.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;

@RegisterVRDecorator
public class DecoratorEmpty extends VRDecorator {
    public static final String ID = "empty";

    public DecoratorEmpty(@NotNull VisorAddon owner) {
        super(owner, ID);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onExit() {

    }

    @Override
    public void tick() {

    }

    @Override
    public void render(PoseStack poseStack, float partialTicks) {

    }

    @Override
    public boolean canActivate() {
        return true;
    }

    @Override
    public @NotNull ElementPriority getPriority() {
        return ElementPriority.LOWEST;
    }
}
