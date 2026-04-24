package org.vmstudio.visor.api.client.render.decoration.hand;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.addon.component.PrioritySupporter;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.VisorComponent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;


/**
 * Modifies the vanilla pose for hand item in VR hands (including player model)
 */
public abstract class VRHandItemPose implements VisorComponent, PrioritySupporter {
    @Getter
    private final VisorAddon owner;

    @Getter @Setter
    private boolean enabled = true;

    public VRHandItemPose(@NotNull VisorAddon owner){
        this.owner = owner;
    }


    public abstract void applyPose(@NotNull PoseStack poseStack,
                                   @NotNull AbstractClientPlayer player,
                                   @NotNull HandType hand,
                                   @NotNull ItemStack itemStack,
                                   float equippedProgress,
                                   float partialTick);

    public abstract boolean canApplyPose(@NotNull AbstractClientPlayer player,
                                         @NotNull HandType hand,
                                         @NotNull ItemStack itemStack);



    public boolean isEnabledAndCanApplyPose(@NotNull AbstractClientPlayer player,
                                            @NotNull HandType hand,
                                            @NotNull ItemStack itemStack){
        return enabled && canApplyPose(player, hand, itemStack);
    }


    @Override
    public @NotNull ComponentPriority getPriority() {
        return ComponentPriority.NORMAL;
    }


}
