package org.vmstudio.visor.mixin.client.player;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
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
    @Redirect( method = "maybeBackOffFromEdge",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;move(DDD)Lnet/minecraft/world/phys/AABB;"))
    private AABB visor$keepEdgeProbeTop(AABB instance,
                                        double x,
                                        double y,
                                        double z) {
        if(!VisorState.get().isActive()){
            return instance.move(x, y, z);
        }
        if((Object) this != Minecraft.getInstance().player){
            return instance.move(x, y, z);
        }

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
