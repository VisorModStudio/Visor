package org.vmstudio.visor.api.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.client.render.decoration.hand.HandRenderState;
import org.vmstudio.visor.api.common.HandType;

public interface VRDecorationRenderer {


    /**
     * Direct render call — used when no level exists
     * (e.g. main menu) and pipeline events don't fire.
     *
     * <p>When a level exists, rendering is handled by
     * the mod-loader pipeline stages instead.</p>
     */
    void renderMainMenu(PoseStack poseStack, float partialTicks);

    /**
     * Tick VR decorations
     */
    void tick();


    /**
     * Get render state for hand
     *
     * @param handType the hand type
     * @return the hand render state
     */
    //@TODO third person?
    @NotNull
    HandRenderState getHandState(@NotNull HandType handType);

    /**
     * Get currently rendered decorator
     *
     * @return the decorator instance
     */
    @NotNull VRDecorator getCurrentDecorator();

    /**
     * Get decorator of specified ID
     *
     * @param id the id
     * @return the decorator
     */
    @Nullable VRDecorator getDecorator(@NotNull String id);



}