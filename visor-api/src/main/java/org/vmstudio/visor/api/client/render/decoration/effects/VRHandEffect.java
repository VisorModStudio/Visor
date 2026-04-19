package org.vmstudio.visor.api.client.render.decoration.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;

import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.VisorComponent;
import org.jetbrains.annotations.NotNull;

public abstract class VRHandEffect implements VisorComponent {
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
     * @param renderPass current VR render pass
     * @param poseStack used poseStack
     * @param guiHand if hand is without skin (main menu)
     * @param partialTicks current partialTick
     */
    public abstract void render(@NotNull HandType hand,
                                @NotNull VRRenderPass renderPass,
                                @NotNull PoseStack poseStack,
                                boolean guiHand,
                                float partialTicks);


    public abstract boolean isVisible(@NotNull VRDecorator currentDecorator,
                                      @NotNull HandType hand,
                                      boolean guiHand);

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




    public boolean isEnabledAndVisible(@NotNull VRDecorator currentDecorator,
                                       @NotNull HandType hand,
                                       boolean guiHand){
        return enabled && isVisible(currentDecorator, hand, guiHand);
    }
}
