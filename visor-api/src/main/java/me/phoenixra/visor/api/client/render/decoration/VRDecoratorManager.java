package me.phoenixra.visor.api.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface VRDecoratorManager {


    @NotNull VisorElementRegistry<VRDecorator> getRegistry();


    @NotNull VRDecorator getCurrentDecorator();

    @Nullable VRDecorator getDecorator(@NotNull String id);


    void render(PoseStack poseStack, float partialTicks);
    void tick();
}
