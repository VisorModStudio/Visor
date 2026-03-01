package org.vmstudio.visor.api.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VRDecorationRenderer {


    @NotNull VRDecorator getCurrentDecorator();

    @Nullable VRDecorator getDecorator(@NotNull String id);


    /**
     * Direct render call — used when no level exists
     * (main menu) and pipeline events don't fire.
     *
     * <p>When a level exists, rendering is handled by
     * the mod-loader pipeline stages instead.</p>
     */
    void render(PoseStack poseStack, float partialTicks);
    void tick();

}