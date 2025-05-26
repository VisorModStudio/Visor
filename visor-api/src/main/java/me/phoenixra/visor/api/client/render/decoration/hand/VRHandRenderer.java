package me.phoenixra.visor.api.client.render.decoration.hand;


import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import org.jetbrains.annotations.NotNull;

public interface VRHandRenderer {

    @NotNull
    VisorElementRegistry<VRHandItemPose> getHandItemPosesRegistry();
}
