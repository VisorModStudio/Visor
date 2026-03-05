package org.vmstudio.visor.api.client.render.decoration.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;

import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.VisorComponent;
import org.jetbrains.annotations.NotNull;

public abstract class VRGameEffect implements VisorComponent {
    @Getter
    private final VisorAddon owner;

    @Getter @Setter
    private boolean enabled = true;

    public VRGameEffect(@NotNull VisorAddon owner){
        this.owner = owner;
    }

    public abstract boolean isVisible(@NotNull VRDecorator currentDecorator);

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

    public abstract void render(@NotNull VRRenderPass renderPass,
                                @NotNull PoseStack poseStack,
                                float partialTicks);


    public boolean isEnabledAndVisible(@NotNull VRDecorator currentDecorator){
        return enabled && isVisible(currentDecorator);
    }
}
