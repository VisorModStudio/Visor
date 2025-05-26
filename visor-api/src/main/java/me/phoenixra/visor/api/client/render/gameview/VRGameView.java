package me.phoenixra.visor.api.client.render.gameview;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.common.addon.VisorElement;

public interface VRGameView extends VisorElement {

    void onEnter();
    void onExit();

    void tick();
    void render(PoseStack poseStack,
                float partialTicks);

}
