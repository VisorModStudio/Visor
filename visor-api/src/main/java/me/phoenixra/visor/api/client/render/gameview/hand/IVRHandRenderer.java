package me.phoenixra.visor.api.client.render.gameview.hand;


import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import org.jetbrains.annotations.NotNull;

public interface IVRHandRenderer {

    @NotNull
    VisorElementRegistry<VRHandItemPose> getHandItemPosesRegistry();
}
