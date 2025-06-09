package me.phoenixra.visor.api.client.render.decoration.hand;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.PrioritySupporter;
import me.phoenixra.visor.api.common.addon.VisorElement;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

//@TODO move to player rendering cause it affects to remotePlayer as well
public interface VRHandItemPose extends VisorElement, PrioritySupporter {

    /**
     * Apply pose for an item.
     * <br><br>
     * Return false if given item is not what you want to modify,
     * so, pose with lower priority will be applied instead
     *
     * @return if successfully applied pose
     */
     boolean applyPose(
             @NotNull AbstractClientPlayer player,
             @NotNull ControllerHand hand,
             @NotNull ItemStack itemStack,
             @NotNull PoseStack poseStack,
             float equippedProgress,
             float partialTick
    );


}
