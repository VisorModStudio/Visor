package me.phoenixra.visor.api.client.render.decoration.effects.hand;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.client.render.decoration.effects.VREffect;
import me.phoenixra.visor.api.common.ControllerHand;
import org.jetbrains.annotations.NotNull;

public interface VRHandEffect extends VREffect {

    /**
     * Render hand effect
     *
     * @param hand for which hand render the effect
     * @param renderDisplay current rendering display
     * @param poseStack used poseStack
     * @param simpleHand if hand is without skin (main menu)
     * @param partialTicks current partialTick
     */
    void render(@NotNull ControllerHand hand,
                @NotNull VRDisplay renderDisplay,
                @NotNull PoseStack poseStack,
                boolean simpleHand,
                float partialTicks);

    boolean isVisible(ControllerHand hand, boolean simpleHand);

    default HandRenderStage renderAtStage(){
        return HandRenderStage.AFTER_RENDERED;
    }

}
