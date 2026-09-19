// #!MC-VERSION:: 1.20.6+
package org.vmstudio.visor.mixin.client.player;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.vmstudio.visor.core.client.VisorState;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {


    protected PlayerMixin(EntityType<? extends LivingEntity> entityType,
                          Level level
    ) {
        super(entityType, level);
    }

    // the probe box keeps its original top, so a raised step height cannot make it
    // catch on whatever sits above the ledge
    // 1.20.5 canFallAtLeast already probes only below the feet
}
