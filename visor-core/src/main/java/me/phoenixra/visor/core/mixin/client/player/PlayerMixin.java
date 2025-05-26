package me.phoenixra.visor.core.mixin.client.player;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {


    protected PlayerMixin(EntityType<? extends LivingEntity> entityType,
                          Level level
    ) {
        super(entityType, level);
    }

    /**
     * Fixes issue with maxStepUp size of 1
     * @param instance s
     * @param x s
     * @param y s
     * @param z s
     * @return s
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;move(DDD)Lnet/minecraft/world/phys/AABB;"), method = "maybeBackOffFromEdge")
    private AABB visor$moveSidewaysExtendDown(AABB instance,
                                             double x,
                                             double y,
                                             double z) {
        return new AABB(
                instance.minX + x,
                instance.minY + y,
                instance.minZ + z,
                instance.maxX + x,
                instance.maxY,
                instance.maxZ + z
        );
    }
}
