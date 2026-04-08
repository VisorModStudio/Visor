package org.vmstudio.visor.mixin.common.player;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.server.VRServerSettings;

@Mixin(Player.class)
public abstract class Common_PlayerMixin extends Common_LivingEntityMixin {


    @Shadow public AbstractContainerMenu containerMenu;

    @Shadow public abstract Abilities getAbilities();

    @Shadow public abstract SoundSource getSoundSource();

    @Shadow public abstract boolean tryToStartFallFlying();

    @Shadow public abstract void remove(Entity.RemovalReason reason);

    @Shadow protected abstract float getBlockSpeedFactor();

    @WrapMethod(method = "sweepAttack")
    protected void visor$wrapSweepAttack(Operation<Void> original) {
        original.call();
    }
    @Inject(method = "die", at = @At("TAIL"))
    protected void visor$afterDie(DamageSource damageSource, CallbackInfo ci){

    }


    /* ************************* *\
  //--------OFFHAND SUPPORT--------\\
    \* ************************* */
    @Redirect(method = "getDestroySpeed", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack visor$getActiveHandItem1(Player player) {
        if (!VRServerSettings.isOffhandUsable()) {
            return player.getMainHandItem();
        }
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(player);
        if (vrPlayer == null){
            return player.getMainHandItem();
        }

        if (vrPlayer.getActiveHand() == HandType.OFFHAND) {
            return player.getOffhandItem();
        } else {
            return player.getMainHandItem();
        }
    }

    @Redirect(method = "blockActionRestricted", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack visor$getActiveHandItem2(Player player) {
        if (!VRServerSettings.isOffhandUsable()) {
            return player.getMainHandItem();
        }
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(player);
        if (vrPlayer == null) {
            return player.getMainHandItem();
        }

        if (vrPlayer.getActiveHand() == HandType.OFFHAND) {
            return player.getOffhandItem();
        } else {
            return player.getMainHandItem();
        }
    }

    @Inject(at = @At("HEAD"), method = "hasCorrectToolForDrops",
            cancellable = true)
    public void visor$hasCorrectToolForDrops(BlockState blockState,
                                             CallbackInfoReturnable<Boolean> ci
    ) {
        if (!VRServerSettings.isOffhandUsable()) {
            return;
        }
        Player player = (Player) (Object) this;
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(player);
        if (vrPlayer == null) {
            return;
        }

        if (vrPlayer.getActiveHand() == HandType.OFFHAND) {
            ci.setReturnValue(!blockState.requiresCorrectToolForDrops()
                    || vrPlayer.getMcPlayer().getOffhandItem()
                    .isCorrectToolForDrops(blockState));
        }
    }

}
