package me.phoenixra.visor.mixin.common.world.item;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.Vanishable;
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
public abstract class BowItemMixin extends ProjectileWeaponItem implements Vanishable {


    @Unique
    private static LivingEntity visor$lastShooter;
    public BowItemMixin(Properties properties) {
        super(properties);
    }



    @Inject(method = "releaseUsing", at = @At("HEAD"))
    public void visor$releaseUsing(ItemStack itemStack,
                                  Level level,
                                  LivingEntity livingEntity,
                                  int i,
                                  CallbackInfo callbackInfo) {
        if (livingEntity instanceof Player player) {
            visor$lastShooter = player;
        }

    }

    @Inject(method = "getPowerForTime", at = @At("HEAD"), cancellable = true)
    private static void visor$getPowerForTime(int i,
                                             CallbackInfoReturnable<Float> cir) {
        if (!(visor$lastShooter instanceof ServerPlayer player)){
            return;
        }

        VRServerPlayer vrServerPlayer = VisorAPI.server()
                .getVrPlayer(player);
        if (vrServerPlayer == null || !vrServerPlayer.isVr()) {
            return;
        }

        float power = vrServerPlayer.getBowTension();
        if (power > 1) {
            power = 1;
        }
        cir.setReturnValue(power);

    }
}
