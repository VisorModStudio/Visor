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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
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
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.common.CommonUtils;
import org.vmstudio.visor.extensions.common.PlayerExtension;

import java.util.Objects;

@Mixin(Player.class)
public abstract class Common_PlayerMixin extends Common_LivingEntityMixin
        implements PlayerExtension {


    @Shadow
    public AbstractContainerMenu containerMenu;

    @Unique
    private HandType visor$swingHand = null;



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

    // 1. replace getMainHand with getItemInHand()
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack visor$mainHandItem(Player self, Operation<ItemStack> original) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(self);
        if (vrPlayer == null) {
            return original.call(self);
        }
        return self.getItemInHand(
                Objects.requireNonNullElseGet(
                        visor$swingHand,
                        vrPlayer::getActiveHand
                ).asInteractionHand()
        );

    }

    // 2. getItemInHand()
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack visor$itemInHand(Player self, InteractionHand hand, Operation<ItemStack> original) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(self);
        if (vrPlayer == null) {
            return original.call(self, hand);
        }
        return original.call(
                self,
                Objects.requireNonNullElseGet(
                        visor$swingHand,
                        vrPlayer::getActiveHand
                ).asInteractionHand()
        );

    }

    // 3. ATTACK_DAMAGE attribute for offhand
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D"))
    private double visor$attackDamage(Player self, Attribute attribute, Operation<Double> original) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(self);
        if (vrPlayer == null) {
            return original.call(self, attribute);
        }
        if(visor$swingHand == HandType.OFFHAND
                || vrPlayer.getActiveHand() == HandType.OFFHAND){
            return visor$withOffhandAttributes(() -> original.call(self, attribute));
        }
        return original.call(self, attribute);
    }

    // 4. EnchantmentHelper for offhand
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
        if(visor$swingHand == HandType.OFFHAND
                || vrPlayer.getActiveHand() == HandType.OFFHAND){
            return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, self.getOffhandItem());
        }
        return original.call(selfEntity);
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
