package org.vmstudio.visor.extensions.common;

import net.minecraft.world.damagesource.DamageSource;

public interface ServerPlayerExtension {

    boolean visor$poseBlocks(DamageSource damageSource, boolean alreadyBlocked);

    void visor$setRotationYCached(float value);

    float visor$getRotationYCached();

    void visor$setOffhandSlotCached(int slot);
    int visor$getOffhandSlotCached();

}
