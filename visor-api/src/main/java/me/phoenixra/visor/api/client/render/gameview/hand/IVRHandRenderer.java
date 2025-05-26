package me.phoenixra.visor.api.client.render.gameview.hand;


import me.phoenixra.visor.api.common.addon.VRElementRegistry;
import org.jetbrains.annotations.NotNull;

public interface IVRHandRenderer {

    @NotNull
    VRElementRegistry<VRHandItemPose> getHandItemPosesRegistry();
}
