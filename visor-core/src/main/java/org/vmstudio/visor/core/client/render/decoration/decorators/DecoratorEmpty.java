package org.vmstudio.visor.core.client.render.decoration.decorators;

import com.mojang.blaze3d.vertex.PoseStack;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRDecorator;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RegisterVRDecorator
public class DecoratorEmpty extends VRDecorator {
    public static final String ID = "empty";

    public DecoratorEmpty(@NotNull VisorAddon owner) {
        super(owner, ID);
    }

    @Override
    public void tick() {

    }


    @Override
    public boolean canActivate() {
        return true;
    }

    @Override
    public List<String> gameEffects() {
        return List.of();
    }

    @Override
    public List<String> handEffects() {
        return List.of();
    }

    @Override
    public @NotNull ComponentPriority getPriority() {
        return ComponentPriority.LOWEST;
    }
}
