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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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


    @Unique
    protected ItemStack visor$poseBlockItem;
    @Unique
    protected InteractionHand visor$poseBlockHand;

    @WrapMethod(method = "hurtCurrentlyUsedShield")
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

    @Redirect(method = "hurtCurrentlyUsedShield", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getUsedItemHand()Lnet/minecraft/world/InteractionHand;"))
    private InteractionHand visor$poseBlockShieldArm(Player self) {
        return visor$poseBlockHand != null ? visor$poseBlockHand : self.getUsedItemHand();
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
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D"))
    private double visor$attackDamage(Player self, Attribute attribute, Operation<Double> original) {
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
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
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





    @Unique
    private <T> T visor$withOffhandAttributes(java.util.function.Supplier<T> action) {
        Player self = (Player)(Object)this;
        ItemStack main = self.getMainHandItem();
        ItemStack off  = self.getOffhandItem();

        // Strip mainhand modifiers, apply offhand modifiers as if it were mainhand
        if (!main.isEmpty()) {
            self.getAttributes().removeAttributeModifiers(
                    main.getAttributeModifiers(EquipmentSlot.MAINHAND)
            );
        }
        if (!off.isEmpty()) {
            self.getAttributes().addTransientAttributeModifiers(
                    off.getAttributeModifiers(EquipmentSlot.MAINHAND)
            );
        }

        try {
            return action.get();
        } finally {
            // Always restore, even if action.get() threw
            if (!off.isEmpty()) {
                self.getAttributes().removeAttributeModifiers(
                        off.getAttributeModifiers(EquipmentSlot.MAINHAND)
                );
            }
            if (!main.isEmpty()) {
                self.getAttributes().addTransientAttributeModifiers(
                        main.getAttributeModifiers(EquipmentSlot.MAINHAND)
                );
            }
        }
    }


}
