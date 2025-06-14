package me.phoenixra.visor.api.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VRDecorationRenderer {


    @NotNull VRDecorator getCurrentDecorator();

    @Nullable VRDecorator getDecorator(@NotNull String id);


    void render(PoseStack poseStack, float partialTicks);
    void tick();

}
