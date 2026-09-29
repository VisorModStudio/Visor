package org.vmstudio.visor.mixin.common.player;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import net.minecraft.world.entity.player.Abilities;
//? if >=1.21.5 {
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import org.vmstudio.visor.core.common.player.VROffhandEquipment;
//?}
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
//? if >=1.21 {
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
//?} else {
/*import net.minecraft.world.item.enchantment.Enchantments;
*///?}
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
//? if >=1.21.11 {
import net.minecraft.core.particles.ParticleOptions;
//?}
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





    // 1.21.11 folded the particle-only sweepAttack() into doSweepAttack(), next to the sweep damage, so only the
    // particle is wrapped there; NeoForge moves that body into an (..., AABB) overload behind a delegating stub
    //? if >=1.21.11 {
    @WrapOperation(method = {
            "doSweepAttack(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;F)V",
            "doSweepAttack(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;FLnet/minecraft/world/phys/AABB;)V"
    }, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    protected int visor$sweepParticles(ServerLevel level, ParticleOptions particle,
                                       double x, double y, double z, int count,
                                       double xDist, double yDist, double zDist, double speed,
                                       Operation<Integer> original) {
        return original.call(level, particle, x, y, z, count, xDist, yDist, zDist, speed);
    }
    //?} else {
    /*@WrapMethod(method = "sweepAttack")
    protected void visor$wrapSweepAttack(Operation<Void> original) {
        original.call();
    }
    *///?}
    @Inject(method = "die", at = @At("TAIL"))
    protected void visor$afterDie(DamageSource damageSource, CallbackInfo ci){

    }


    @Unique
    protected ItemStack visor$poseBlockItem;
    @Unique
    protected InteractionHand visor$poseBlockHand;

    // the shield item is damaged by the blocks_attacks component since 1.21.5
    //? if <1.21.5 {
    /*@WrapMethod(method = "hurtCurrentlyUsedShield")
    private void visor$damagePoseBlockShield(float damageAmount, Operation<Void> original) {
        if (visor$poseBlockItem == null) {
            original.call(damageAmount);
            return;
        }
        ItemStack held = this.useItem;
        this.useItem = visor$poseBlockItem;
        try {
            original.call(damageAmount);
        } finally {
            this.useItem = held;
            visor$poseBlockItem = null;
            visor$poseBlockHand = null;
        }
    }

    @WrapOperation(method = "hurtCurrentlyUsedShield", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getUsedItemHand()Lnet/minecraft/world/InteractionHand;"))
    private InteractionHand visor$poseBlockShieldArm(Player self, Operation<InteractionHand> original) {
        return visor$poseBlockHand != null ? visor$poseBlockHand : original.call(self);
    }
    *///?}

    // two-handed VR keeps the offhand in a hotbar slot: since 1.21.5 that is the equipment, not an inventory list
    //? if >=1.21.5 {
    @Inject(method = "createEquipment", at = @At("RETURN"), cancellable = true)
    private void visor$offhandEquipment(CallbackInfoReturnable<EntityEquipment> cir) {
        cir.setReturnValue(new VROffhandEquipment((Player) (Object) this));
    }
    //?}

    // Inventory.getDestroySpeed was folded into Player.getDestroySpeed in 1.21.5: the modifiers follow the stack.
    // Forge and NeoForge move that body into a (BlockState, BlockPos) overload and leave a delegating stub
    //? if >=1.21.5 {
    @WrapOperation(method = {
            "getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F",
            "getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)F"
    }, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;getSelectedItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack visor$offhandDestroySpeedItem(Inventory inventory, Operation<ItemStack> original) {
        ItemStack forced = CommonUtils.FORCED_HAND_ITEM.get();
        if (forced != null) {
            return forced;
        }
        if (VRServerSettings.isTwoHandedVR()) {
            Player self = (Player) (Object) this;
            VRPlayer vrPlayer = VisorAPI.getVRPlayer(self);
            if (vrPlayer != null && vrPlayer.getActiveHand() == HandType.OFFHAND) {
                return self.getOffhandItem();
            }
        }
        return original.call(inventory);
    }
    //?}


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


    @Inject(at = @At("HEAD"), method = "hasCorrectToolForDrops",
            cancellable = true)
    public void visor$hasCorrectToolForDrops(BlockState blockState,
                                             CallbackInfoReturnable<Boolean> ci
    ) {
        ItemStack forced = CommonUtils.FORCED_HAND_ITEM.get();
        if (forced != null) {
            ci.setReturnValue(!blockState.requiresCorrectToolForDrops()
                    || forced.isCorrectToolForDrops(blockState));
            return;
        }
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

    //? if >=1.21 {
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
    //?}

    // replace getMainHand with getItemInHand()
    // 1.21+ takes the weapon from getWeaponItem (above) and reads the main hand only to pick the hand it empties when
    // the weapon breaks, so a wrap there emptied the main hand after an offhand weapon broke
    //? if <1.21 {
    /*@WrapOperation(method = "attack", at = @At(value = "INVOKE",
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

    // the weapon above is the attack hand's item, empty that hand when it breaks
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
    private void visor$brokenWeaponHand(Player self, InteractionHand hand, ItemStack stack, Operation<Void> original) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(self);
        original.call(self, vrPlayer == null ? hand : visor$attackHand(vrPlayer).asInteractionHand(), stack);
    }
    *///?}

    //getItemInHand()
    // 1.21.11 moved the sword check into isSweepAttack, Forge also reads the sweep hitbox in doSweepAttack
    //? if >=1.21.11 {
    @WrapOperation(method = {"isSweepAttack(ZZZ)Z", "doSweepAttack(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;F)V"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    //?} else {
    /*@WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    *///?}
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

    //ATTACK_DAMAGE attribute for offhand, and the sweep ratio that 1.21.11 moved into doSweepAttack
    // (both doSweepAttack shapes, see visor$sweepParticles)
    //? if >=1.21.11 {
    @WrapOperation(method = {"attack", "doSweepAttack(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;F)V", "doSweepAttack(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;FLnet/minecraft/world/phys/AABB;)V"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
    private double visor$attackDamage(Player self, Holder<Attribute> attribute, Operation<Double> original) {
    //?} elif >=1.20.5 {
    /*@WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
    private double visor$attackDamage(Player self, Holder<Attribute> attribute, Operation<Double> original) {
    *///?} else {
    /*@WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D"))
    private double visor$attackDamage(Player self, Attribute attribute, Operation<Double> original) {
    *///?}
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(self);
        if (vrPlayer == null) {
            return original.call(self, attribute);
        }
        if(visor$attackHand(vrPlayer) == HandType.OFFHAND){
            return visor$withOffhandAttributes(() -> original.call(self, attribute));
        }
        return original.call(self, attribute);
    }

    // EnchantmentHelper for offhand
    //? if >=1.21 {
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getKnockback(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)F"))
    private float visor$knockback(Player self, Entity target, DamageSource damageSource,
                                  Operation<Float> original) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(self);
        if (vrPlayer == null || visor$attackHand(vrPlayer) != HandType.OFFHAND) {
            return original.call(self, target, damageSource);
        }
        float base = visor$withOffhandAttributes(
                () -> (float) self.getAttributeValue(Attributes.ATTACK_KNOCKBACK)
        );
        float knockback = self.level() instanceof ServerLevel serverLevel
                ? EnchantmentHelper.modifyKnockback(serverLevel, self.getOffhandItem(),
                        target, damageSource, base)
                : base;
        // 1.21.11 halves inside getKnockback, attack no longer does
        //? if >=1.21.11 {
        return knockback / 2.0F;
        //?} else {
        /*return knockback;
        *///?}
    }
    //?} else {
    /*@WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getKnockbackBonus(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int visor$knockback(LivingEntity selfEntity, Operation<Integer> original) {
        if(!(selfEntity instanceof Player self)){
            return original.call(selfEntity);
        }
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(self);
        if (vrPlayer == null) {
            return original.call(selfEntity);
        }
        if(visor$attackHand(vrPlayer) == HandType.OFFHAND){
            return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, self.getOffhandItem());
        }
        return original.call(selfEntity);
    }
    *///?}

    // knockback for living entities targets
    // 1.21.11 moved it into causeExtraKnockback (attack and spear stab), the swept targets into doSweepAttack
    // (both doSweepAttack shapes, see visor$sweepParticles)
    //? if >=1.21.11 {
    @WrapOperation(method = {"causeExtraKnockback(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/phys/Vec3;)V", "doSweepAttack(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;F)V", "doSweepAttack(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;FLnet/minecraft/world/phys/AABB;)V"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    //?} else {
    /*@WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    *///?}
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
    //? if >=1.21.11 {
    @WrapOperation(method = "causeExtraKnockback(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/phys/Vec3;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"))
    //?} else {
    /*@WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"))
    *///?}
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





    @Unique
    private <T> T visor$withOffhandAttributes(java.util.function.Supplier<T> action) {
        Player self = (Player)(Object)this;
        ItemStack main = self.getMainHandItem();
        ItemStack off  = self.getOffhandItem();

        // Strip mainhand modifiers, apply offhand modifiers as if it were mainhand
        if (!main.isEmpty()) {
            McVersionUtils.removeItemAttributeModifiers(self, main, EquipmentSlot.MAINHAND);
        }
        if (!off.isEmpty()) {
            McVersionUtils.addItemAttributeModifiers(self, off, EquipmentSlot.MAINHAND);
        }

        try {
            return action.get();
        } finally {
            // Always restore, even if action.get() threw
            if (!off.isEmpty()) {
                McVersionUtils.removeItemAttributeModifiers(self, off, EquipmentSlot.MAINHAND);
            }
            if (!main.isEmpty()) {
                McVersionUtils.addItemAttributeModifiers(self, main, EquipmentSlot.MAINHAND);
            }
        }
    }


}
