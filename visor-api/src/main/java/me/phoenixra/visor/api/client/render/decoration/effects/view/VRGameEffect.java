package me.phoenixra.visor.api.client.render.decoration.effects.view;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.client.render.decoration.effects.VREffect;
import org.jetbrains.annotations.NotNull;

public interface VRGameEffect extends VREffect {

    boolean isVisible();


    default void render(@NotNull VRDisplay renderDisplay,
                        @NotNull PoseStack poseStack,
                        float partialTicks){

    }

}
