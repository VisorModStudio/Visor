package me.phoenixra.visor.api.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.client.render.decoration.effects.VRGameEffect;
import me.phoenixra.visor.api.client.render.decoration.hand.VRHandRenderer;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VRDecorationRenderer {


    @NotNull VisorElementRegistry<VRDecorator> getRegistry();
    @NotNull VisorElementRegistry<VRGameEffect> getEffectsRegistry();


    @NotNull VRDecorator getCurrentDecorator();

    @Nullable VRDecorator getDecorator(@NotNull String id);


    void render(PoseStack poseStack, float partialTicks);
    void tick();


    /**
     * Get Hands renderer.
     * <br>
     * Can be used to render effects attached
     * to VR hands.
     * @return Hands renderer instance
     */
    @NotNull VRHandRenderer getHandsRenderer();

}
