package org.vmstudio.visor.extensions.client.entity;


import org.vmstudio.visor.api.client.input.HandAction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface LocalPlayerExtension {


    void visor$setUsingItem(ItemStack itemstack1, InteractionHand interactionhand);

    void visor$setUseItemRemaining(int i);

    void visor$setTeleported(boolean flag);

    void visor$stepSound(BlockPos blockpos, Vec3 vec3);



    float visor$getSpeedFactor();
    float visor$getJumpFactor();

    double visor$getRoomYOffset();

}
