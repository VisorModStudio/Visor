package org.vmstudio.visor.core.client.render.player.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;

public interface ControllerSpaceItemAnchorModel {
    void applyLocalHandItemAnchor(HumanoidArm arm, PoseStack poseStack);
}