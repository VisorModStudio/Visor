package me.phoenixra.visor.api.client.render.decoration.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;

import me.phoenixra.visor.api.client.render.VRCameraType;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.element.VisorElement;
import org.jetbrains.annotations.NotNull;

public abstract class VRGameEffect implements VisorElement {
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

    public abstract void render(@NotNull VRCameraType cameraType,
                                @NotNull PoseStack poseStack,
                                float partialTicks);


    public boolean isEnabledAndVisible(@NotNull VRDecorator currentDecorator){
        return enabled && isVisible(currentDecorator);
    }
}
