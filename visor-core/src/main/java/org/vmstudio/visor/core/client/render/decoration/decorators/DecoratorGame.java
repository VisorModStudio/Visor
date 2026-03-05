package org.vmstudio.visor.core.client.render.decoration.decorators;

import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRDecorator;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.render.decoration.effects.hand.HandEffectTeleport;
import org.vmstudio.visor.core.client.render.decoration.effects.GameEffectOnFire;
import org.vmstudio.visor.core.client.render.decoration.effects.GameEffectShadow;
import org.vmstudio.visor.core.client.render.decoration.effects.GameEffectVanilla;
import org.vmstudio.visor.core.client.render.decoration.effects.hand.HandEffectCrosshair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@RegisterVRDecorator
public class DecoratorGame extends VRDecorator {
    public static final String ID = "game";

    public DecoratorGame(@NotNull VisorAddon owner) {
        super(owner, ID);
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean supportsWorldHands() {
        return true;
    }



    @Override
    public boolean canActivate() {
        return MC.player != null && MC.level != null && MC.screen == null;
    }

    @Override
    public List<String> gameEffects() {
        return List.of(
                GameEffectOnFire.ID,
                GameEffectShadow.ID,
                GameEffectVanilla.ID
        );
    }

    @Override
    public List<String> handEffects() {
        return List.of(
                HandEffectCrosshair.ID,
                HandEffectTeleport.ID
        );
    }

    @Override
    public @NotNull ComponentPriority getPriority() {
        return ComponentPriority.LOW;
    }
}