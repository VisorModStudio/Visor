package org.vmstudio.visor.mixin.common.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Shoot power modified
 */
@Mixin(BowItem.class)
public abstract class BowItemMixin extends ProjectileWeaponItem {


    @Unique
    private static LivingEntity visor$lastShooter;
    public BowItemMixin(Properties properties) {
        super(properties);
    }



    @Inject(method = "releaseUsing", at = @At("HEAD"))
    public void visor$releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player) {
            visor$lastShooter = player;
        }

    }
/*
    @Inject(method = "getPowerForTime", at = @At("HEAD"), cancellable = true)
    private static void visor$getPowerForTime(int i,
                                             CallbackInfoReturnable<Float> cir) {
        if (!(visor$lastShooter instanceof ServerPlayer player)){
            return;
        }

        VRServerPlayer vrPlayer = VisorAPI.server()
                .getVrPlayer(player);
        if (vrPlayer == null || !vrPlayer.isVRActive()) {
            return;
        }

        float power = vrPlayer.getBowTension();
        if (power > 1) {
            power = 1;
        }
        cir.setReturnValue(power);

    }*/
}
