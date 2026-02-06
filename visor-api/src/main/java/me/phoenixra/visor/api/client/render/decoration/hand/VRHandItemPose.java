package me.phoenixra.visor.api.client.render.decoration.hand;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.api.common.addon.component.ComponentPriority;
import me.phoenixra.visor.api.common.addon.component.PrioritySupporter;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.component.VisorComponent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;


//@TODO move to remote player rendering maybe or make "general" rendering for VR players?
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
