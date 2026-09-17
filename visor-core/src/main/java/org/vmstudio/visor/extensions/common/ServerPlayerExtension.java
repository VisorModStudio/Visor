package org.vmstudio.visor.extensions.common;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;

public interface ServerPlayerExtension {

    boolean visor$poseBlocks(DamageSource damageSource, boolean alreadyBlocked);

    ItemStack visor$getPoseBlockItem();
    InteractionHand visor$getPoseBlockHand();

    void visor$setRotationYCached(float value);

    float visor$getRotationYCached();

    void visor$setOffhandSlotCached(int slot);
    int visor$getOffhandSlotCached();

}
