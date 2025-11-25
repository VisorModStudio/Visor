package me.phoenixra.visor.api.client.render.decoration.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;

import me.phoenixra.visor.api.client.render.VRCameraType;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.element.VisorElement;
import org.jetbrains.annotations.NotNull;

public abstract class VRHandEffect implements VisorElement {
    @Getter
    private final VisorAddon owner;

    @Getter @Setter
    private boolean enabled = true;

    public VRHandEffect(@NotNull VisorAddon owner){
        this.owner = owner;
    }

    /**
     * Render hand effect
     *
     * @param hand for which hand render the effect
     * @param cameraType current VR camera type
     * @param poseStack used poseStack
     * @param simpleHand if hand is without skin (main menu)
     * @param partialTicks current partialTick
     */
    public abstract void render(@NotNull HandType hand,
                                @NotNull VRCameraType cameraType,
                                @NotNull PoseStack poseStack,
                                boolean simpleHand,
                                float partialTicks);


    public abstract boolean isVisible(@NotNull VRDecorator currentDecorator,
                                      @NotNull HandType hand,
                                      boolean simpleHand);

    /**
     * If effect is allowed to be visible on all decorators.
     *
     * <p>When false, effect can be added
     * to decorator only manually</p>
     *
     * @return trie/false
     */
    public boolean isGlobal(){
        return false;
    }

    public RenderStage renderAtStage(){
        return RenderStage.AFTER_HANDS;
    }



    public boolean isEnabledAndVisible(@NotNull VRDecorator currentDecorator,
                                       @NotNull HandType hand,
                                       boolean simpleHand){
        return enabled && isVisible(currentDecorator, hand, simpleHand);
    }







    public enum RenderStage {
        /**
         * Effect is rendered right before hands
         */
        BEFORE_HANDS,

        /**
         * Effect is rendered right after hands
         */
        AFTER_HANDS,

    }

}
