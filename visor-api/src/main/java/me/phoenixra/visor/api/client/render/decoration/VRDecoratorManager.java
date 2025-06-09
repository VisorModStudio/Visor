package me.phoenixra.visor.api.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.client.render.decoration.effects.view.VRGameEffect;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VRDecoratorManager {


    @NotNull VisorElementRegistry<VRDecorator> getRegistry();
    @NotNull VisorElementRegistry<VRGameEffect> getEffectsRegistry();


    @NotNull VRDecorator getCurrentDecorator();

    @Nullable VRDecorator getDecorator(@NotNull String id);


    void render(PoseStack poseStack, float partialTicks);
    void tick();
}
