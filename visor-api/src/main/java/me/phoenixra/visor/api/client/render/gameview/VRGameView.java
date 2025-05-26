package me.phoenixra.visor.api.client.render.gameview;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.common.addon.VRElement;

public interface VRGameView extends VRElement {

    void onEnter();
    void onExit();

    void tick();
    void render(PoseStack poseStack,
                float partialTicks);

}
