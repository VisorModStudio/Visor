package org.vmstudio.visor.mixin.common.player;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.core.common.CommonUtils;
import org.vmstudio.visor.extensions.common.PlayerExtension;


@Mixin(Player.class)
public abstract class Common_PlayerMixin extends Common_LivingEntityMixin
        implements PlayerExtension {


    @Shadow
    public AbstractContainerMenu containerMenu;

    @Unique
    protected HandType visor$swingHand = null;



    @Shadow
    public abstract Abilities getAbilities();
    @Shadow
    public abstract SoundSource getSoundSource();
    @Shadow
    public abstract boolean tryToStartFallFlying();
    @Shadow
    public abstract void remove(Entity.RemovalReason reason);
    @Shadow
    protected abstract float getBlockSpeedFactor();





    @WrapMethod(method = "sweepAttack")
    protected void visor$wrapSweepAttack(Operation<Void> original) {
        original.call();
    }
    @Inject(method = "die", at = @At("TAIL"))
    protected void visor$afterDie(DamageSource damageSource, CallbackInfo ci){

    }

    @WrapMethod(method = "hurtCurrentlyUsedShield")
    protected void visor$roomscaleShieldItemDamage(float damageAmount, Operation<Void> original) {
        original.call(damageAmount);
    }

    @ModifyExpressionValue(method = "hurtCurrentlyUsedShield",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getUsedItemHand()Lnet/minecraft/world/InteractionHand;"))
    protected InteractionHand visor$roomscaleShieldHand(InteractionHand original) {
        return original;
    }


    /* ***************************************** *\
  //--------TWO HANDED VR (OFFHAND SUPPORT)--------\\
    \* ***************************************** */
    @WrapOperation(method = "blockActionRestricted",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack visor$forceHandInBlockRestricted(Player self, Operation<ItemStack> original) {
        ItemStack forced = CommonUtils.FORCED_HAND_ITEM.get();
        return forced != null ? forced : original.call(self);
    }

    // getDestroySpeed → inventory.getDestroySpeed: route the inventory lookup to the forced item
    @WrapOperation(method = "getDestroySpeed",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F"))
    private float visor$forceInventoryDestroySpeed(Inventory inv, BlockState state, Operation<Float> original) {
        ItemStack forced = CommonUtils.FORCED_HAND_ITEM.get();
        if (forced != null && !forced.isEmpty()) {
            return forced.getDestroySpeed(state);
        }
        return original.call(inv, state);
    }

    @Inject(at = @At("HEAD"), method = "hasCorrectToolForDrops",
            cancellable = true)
    public void visor$hasCorrectToolForDrops(BlockState blockState,
                                             CallbackInfoReturnable<Boolean> ci
    ) {
        if (!VRServerSettings.isTwoHandedVR()) {
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



    /* ***************************************** *\
  //--------BETTER SWINGING + TWO HANDED VR--------\\
    \* ***************************************** */
    @Override @Unique
    public void visor$swingAttack(Entity entity, HandType handType) {
        Player self = (Player)(Object)this;
        if (handType == HandType.MAIN) {
            visor$swingHand = HandType.MAIN;
            self.attack(entity);
            visor$swingHand = null;
            return;
        }
        visor$swingHand = HandType.OFFHAND;
        try {
            self.attack(entity);
        } finally {
            visor$swingHand = null;
        }
    }

    // common method to resolve edge cases like with shield,
    // when we want to swing with shield raised and item should not be used as shield obviously
    @Unique
    protected HandType visor$attackHand(VRPlayer vrPlayer) {
        if (visor$swingHand != null) {
            return visor$swingHand;
        }
        HandType hand = vrPlayer.getActiveHand();
        Player self = (Player) (Object) this;
        if (self.isBlocking() && hand.asInteractionHand() == self.getUsedItemHand()) {
            return hand.opposite();
        }
        return hand;
    }


    @Inject(method = "getWeaponItem", at = @At("HEAD"), cancellable = true)
    private void visor$vrWeaponItem(CallbackInfoReturnable<ItemStack> cir) {
        if (!VRServerSettings.isTwoHandedVR()) {
            return;
        }
        Player self = (Player) (Object) this;
        if (self.isAutoSpinAttack()) {
            return;
        }
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(self);
        if (vrPlayer == null) {
            return;
        }
        if (visor$attackHand(vrPlayer) == HandType.OFFHAND) {
            cir.setReturnValue(self.getOffhandItem());
        }
    }

    // replace getMainHand with getItemInHand()
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack visor$mainHandItem(Player self, Operation<ItemStack> original) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(self);
        if (vrPlayer == null) {
            return original.call(self);
        }
        return self.getItemInHand(
                visor$attackHand(vrPlayer).asInteractionHand()
        );

    }

    //getItemInHand()
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack visor$itemInHand(Player self, InteractionHand hand, Operation<ItemStack> original) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(self);
        if (vrPlayer == null) {
            return original.call(self, hand);
        }
        return original.call(
                self,
                visor$attackHand(vrPlayer).asInteractionHand()
        );

    }

    //ATTACK_DAMAGE attribute for offhand
    // 1.21.1: attributes are Holder-based now
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
    private double visor$attackDamage(Player self, Holder<Attribute> attribute, Operation<Double> original) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(self);
        if (vrPlayer == null) {
            return original.call(self, attribute);
        }
        if(visor$attackHand(vrPlayer) == HandType.OFFHAND){
            return visor$withOffhandAttributes(() -> original.call(self, attribute));
        }
        return original.call(self, attribute);
    }


    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getKnockback(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)F"))
    private float visor$knockback(Player self, Entity target, DamageSource damageSource, Operation<Float> original) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(self);
        if (vrPlayer == null || visor$attackHand(vrPlayer) != HandType.OFFHAND) {
            return original.call(self, target, damageSource);
        }
        float base = (float) self.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        return self.level() instanceof ServerLevel serverLevel
                ? EnchantmentHelper.modifyKnockback(serverLevel, self.getOffhandItem(), target, damageSource, base)
                : base;
    }

    // knockback for living entities targets
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    private void visor$vrKnockbackDirection(LivingEntity target, double strength, double x, double z,
                                            Operation<Void> original) {
        Vec3 knockBack = CommonUtils.calcVRKnockback((Player) (Object) this, target);
        if (knockBack != null) {
            x = knockBack.x;
            z = knockBack.z;
        }
        original.call(target, strength, x, z);
    }

    // knockback for non-living entities targets
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"))
    private void visor$vrPushDirection(Entity target, double x, double y, double z,
                                       Operation<Void> original) {
        Vec3 knockBack = CommonUtils.calcVRKnockback((Player) (Object) this, target);
        if (knockBack != null) {
            double strength = Math.sqrt(x * x + z * z);
            x = -knockBack.x * strength;
            z = -knockBack.z * strength;
        }
        original.call(target, x, y, z);
    }





    /**
     * 1.21.1: ItemStack#getAttributeModifiers(EquipmentSlot) is gone;
     * the modifiers are collected through forEachModifier instead
     */
    @Unique
    private static Multimap<Holder<Attribute>, AttributeModifier> visor$mainHandModifiers(ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
        stack.forEachModifier(EquipmentSlot.MAINHAND, modifiers::put);
        return modifiers;
    }

    @Unique
    private <T> T visor$withOffhandAttributes(java.util.function.Supplier<T> action) {
        Player self = (Player)(Object)this;
        ItemStack main = self.getMainHandItem();
        ItemStack off  = self.getOffhandItem();

        // Strip mainhand modifiers, apply offhand modifiers as if it were mainhand
        if (!main.isEmpty()) {
            self.getAttributes().removeAttributeModifiers(
                    visor$mainHandModifiers(main)
            );
        }
        if (!off.isEmpty()) {
            self.getAttributes().addTransientAttributeModifiers(
                    visor$mainHandModifiers(off)
            );
        }

        try {
            return action.get();
        } finally {
            // Always restore, even if action.get() threw
            if (!off.isEmpty()) {
                self.getAttributes().removeAttributeModifiers(
                        visor$mainHandModifiers(off)
                );
            }
            if (!main.isEmpty()) {
                self.getAttributes().addTransientAttributeModifiers(
                        visor$mainHandModifiers(main)
                );
            }
        }
    }


}
