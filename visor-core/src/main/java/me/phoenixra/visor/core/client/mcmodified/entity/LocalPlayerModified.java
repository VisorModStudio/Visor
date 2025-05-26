package me.phoenixra.visor.core.client.mcmodified.entity;


import me.phoenixra.visor.api.client.input.HandAction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface LocalPlayerModified {


    void visor$setUsingItem(ItemStack itemstack1, InteractionHand interactionhand);

    void visor$setUseItemRemaining(int i);


    void visor$stepSound(BlockPos blockpos, Vec3 vec3);

    void visor$swingArm(InteractionHand interactionhand, HandAction interact);


    float visor$getJumpFactor();

    double visor$getRoomYOffset();

}
