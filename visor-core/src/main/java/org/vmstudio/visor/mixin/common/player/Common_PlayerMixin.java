package org.vmstudio.visor.mixin.common.player;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
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
import org.vmstudio.visor.core.client.utils.EnchantmentVisitor;
import org.vmstudio.visor.core.common.CommonUtils;
import org.vmstudio.visor.extensions.common.PlayerExtension;

import java.util.Iterator;
import java.util.List;

@Mixin(Player.class)
public abstract class Common_PlayerMixin extends Common_LivingEntityMixin
        implements PlayerExtension {


    @Shadow
    public AbstractContainerMenu containerMenu;

    @Unique
    private boolean visor$swingOverride = false;



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



    /* ************************* *\
  //--------BETTER SWINGING--------\\
    \* ************************* */
    @Override @Unique
    public void visor$swingAttack(Entity entity, HandType handType) {
        Player thisPlayer = (Player) (Object) this;
        if (!entity.isAttackable()
                || entity.skipAttackInteraction(thisPlayer)) {
            return;
        }


        ItemStack handItem = handType == HandType.MAIN ?
                thisPlayer.getMainHandItem() : thisPlayer.getOffhandItem();

        //change attributes to match offhand item damage
        if(handType != HandType.MAIN){
            if (!thisPlayer.getMainHandItem().isEmpty()) {
                thisPlayer.getAttributes().removeAttributeModifiers(
                        thisPlayer.getMainHandItem().getAttributeModifiers(
                                EquipmentSlot.MAINHAND
                        )
                );
            }

            if (!thisPlayer.getOffhandItem().isEmpty()) {
                thisPlayer.getAttributes().addTransientAttributeModifiers(
                        thisPlayer.getOffhandItem().getAttributeModifiers(
                                EquipmentSlot.MAINHAND
                        )
                );
            }
        }

        float damage = (float)thisPlayer.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float enchantDamage;

        //get back to main
        if(handType != HandType.MAIN){
            if (!thisPlayer.getOffhandItem().isEmpty()) {
                thisPlayer.getAttributes().removeAttributeModifiers(
                        thisPlayer.getOffhandItem().getAttributeModifiers(
                                EquipmentSlot.MAINHAND
                        )
                );
            }

            if (!thisPlayer.getMainHandItem().isEmpty()) {
                thisPlayer.getAttributes().addTransientAttributeModifiers(
                        thisPlayer.getMainHandItem().getAttributeModifiers(
                                EquipmentSlot.MAINHAND
                        )
                );
            }
        }


        if (entity instanceof LivingEntity) {
            enchantDamage = EnchantmentHelper.getDamageBonus(handItem, ((LivingEntity)entity).getMobType());
        } else {
            enchantDamage = EnchantmentHelper.getDamageBonus(handItem, MobType.UNDEFINED);
        }

        float h = thisPlayer.getAttackStrengthScale(0.5F);
        damage *= 0.2F + h * h * 0.8F;
        enchantDamage *= h;
        thisPlayer.resetAttackStrengthTicker();
        if (damage > 0.0F || enchantDamage > 0.0F) {
            boolean bl = h > 0.9F;
            boolean bl2 = false;
            int knockback = 0;
            knockback += EnchantmentHelper.getItemEnchantmentLevel(
                    Enchantments.KNOCKBACK, handItem);
            if (thisPlayer.isSprinting() && bl) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, this.getSoundSource(), 1.0F, 1.0F);
                ++knockback;
                bl2 = true;
            }

            boolean bl3 = bl && thisPlayer.fallDistance > 0.0F && !this.onGround() && !thisPlayer.onClimbable() && !thisPlayer.isInWater() && !thisPlayer.hasEffect(MobEffects.BLINDNESS) && !this.isPassenger() && entity instanceof LivingEntity;
            bl3 = bl3 && !thisPlayer.isSprinting();
            if (bl3) {
                damage *= 1.5F;
            }

            damage += enchantDamage;
            boolean bl4 = false;
            double d = thisPlayer.walkDist - thisPlayer.walkDistO;
            if (bl && !bl3 && !bl2 && this.onGround() && d < (double)thisPlayer.getSpeed()) {
                ItemStack itemStack = handType == HandType.MAIN ?
                        thisPlayer.getMainHandItem() : thisPlayer.getOffhandItem();
                if (itemStack.getItem() instanceof SwordItem) {
                    bl4 = true;
                }
            }

            float j = 0.0F;
            boolean bl5 = false;
            int k = EnchantmentHelper.getItemEnchantmentLevel(
                    Enchantments.FIRE_ASPECT, handItem);
            if (entity instanceof LivingEntity) {
                j = ((LivingEntity)entity).getHealth();
                if (k > 0 && !entity.isOnFire()) {
                    bl5 = true;
                    entity.setSecondsOnFire(1);
                }
            }

            Vec3 vec3 = entity.getDeltaMovement();
            boolean bl6 = entity.hurt(thisPlayer.damageSources().playerAttack(thisPlayer), damage);
            if (bl6) {
                if (knockback > 0) {
                    if (entity instanceof LivingEntity) {
                        ((LivingEntity)entity).knockback((double)((float)knockback * 0.5F), (double) Mth.sin(this.getYRot() * Mth.DEG_TO_RAD), (double)(-Mth.cos(this.getYRot() * Mth.DEG_TO_RAD)));
                    } else {
                        entity.push((double)(-Mth.sin(this.getYRot() * Mth.DEG_TO_RAD) * (float)knockback * 0.5F), 0.1, (double)(Mth.cos(this.getYRot() * Mth.DEG_TO_RAD) * (float)knockback * 0.5F));
                    }

                    thisPlayer.setDeltaMovement(thisPlayer.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                    thisPlayer.setSprinting(false);
                }

                if (bl4) {
                    float l = 1.0F + EnchantmentHelper.getItemEnchantmentLevel(
                            Enchantments.SWEEPING_EDGE, handItem) * damage;
                    List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(1.0, 0.25, 1.0));
                    Iterator var19 = list.iterator();

                    label166:
                    while(true) {
                        LivingEntity livingEntity;
                        do {
                            do {
                                do {
                                    do {
                                        if (!var19.hasNext()) {
                                            this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, this.getSoundSource(), 1.0F, 1.0F);
                                            thisPlayer.sweepAttack();
                                            break label166;
                                        }

                                        livingEntity = (LivingEntity)var19.next();
                                    } while(livingEntity == thisPlayer);
                                } while(livingEntity == entity);
                            } while(thisPlayer.isAlliedTo(livingEntity));
                        } while(livingEntity instanceof ArmorStand && ((ArmorStand)livingEntity).isMarker());

                        if (thisPlayer.distanceToSqr(livingEntity) < 9.0) {
                            livingEntity.knockback(0.4000000059604645, (double)Mth.sin(this.getYRot() * Mth.DEG_TO_RAD), (double)(-Mth.cos(this.getYRot() * Mth.DEG_TO_RAD)));
                            livingEntity.hurt(thisPlayer.damageSources().playerAttack(thisPlayer), l);
                        }
                    }
                }

                if (entity instanceof ServerPlayer && entity.hurtMarked) {
                    ((ServerPlayer)entity).connection.send(new ClientboundSetEntityMotionPacket(entity));
                    entity.hurtMarked = false;
                    entity.setDeltaMovement(vec3);
                }

                if (bl3) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, this.getSoundSource(), 1.0F, 1.0F);
                    thisPlayer.crit(entity);
                }

                if (!bl3 && !bl4) {
                    if (bl) {
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, this.getSoundSource(), 1.0F, 1.0F);
                    } else {
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, this.getSoundSource(), 1.0F, 1.0F);
                    }
                }

                if (enchantDamage > 0.0F) {
                    thisPlayer.magicCrit(entity);
                }

                thisPlayer.setLastHurtMob(entity);
                if (entity instanceof LivingEntity) {
                    visor$doPostHurtEffects((LivingEntity)entity,
                            thisPlayer,
                            handType == HandType.MAIN ?
                                    thisPlayer.getMainHandItem() : thisPlayer.getOffhandItem());
                }
                visor$doPostDamageEffects(
                        thisPlayer,
                        entity,
                        handType == HandType.MAIN ?
                                thisPlayer.getMainHandItem() : thisPlayer.getOffhandItem()
                );
                ItemStack itemStack2 = handType == HandType.MAIN ?
                        thisPlayer.getMainHandItem() : thisPlayer.getOffhandItem();
                Entity entity2 = entity;
                if (entity instanceof EnderDragonPart) {
                    entity2 = ((EnderDragonPart)entity).parentMob;
                }

                if (!this.level().isClientSide && !itemStack2.isEmpty() && entity2 instanceof LivingEntity) {
                    itemStack2.hurtEnemy((LivingEntity)entity2, thisPlayer);
                    if (itemStack2.isEmpty()) {
                        thisPlayer.setItemInHand(handType == HandType.MAIN ?
                                        InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                                ItemStack.EMPTY);
                    }
                }

                if (entity instanceof LivingEntity) {
                    float m = j - ((LivingEntity)entity).getHealth();
                    thisPlayer.awardStat(Stats.DAMAGE_DEALT, Math.round(m * 10.0F));
                    if (k > 0) {
                        entity.setSecondsOnFire(k * 4);
                    }

                    if (this.level() instanceof ServerLevel && m > 2.0F) {
                        int n = (int)((double)m * 0.5);
                        ((ServerLevel)this.level()).sendParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(), entity.getY(0.5), entity.getZ(), n, 0.1, 0.0, 0.1, 0.2);
                    }
                }

                thisPlayer.causeFoodExhaustion(0.1F);
            } else {
                this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource(), 1.0F, 1.0F);
                if (bl5) {
                    entity.clearFire();
                }
            }
        }
    }

    @Unique
    private void visor$doPostHurtEffects(LivingEntity livingEntity,
                                         Entity entity, ItemStack handItem) {
        EnchantmentVisitor enchantmentVisitor = (enchantment, i) -> {
            enchantment.doPostHurt(livingEntity, entity, i);
        };
        if (livingEntity != null) {
            visor$runIterationOnInventory(enchantmentVisitor, livingEntity.getAllSlots());
        }

        if (entity instanceof Player) {
            visor$runIterationOnItem(enchantmentVisitor, handItem);
        }

    }
    @Unique
    private void visor$doPostDamageEffects(LivingEntity livingEntity, Entity entity, ItemStack handItem) {
        EnchantmentVisitor enchantmentVisitor = (enchantment, i) -> {
            enchantment.doPostAttack(livingEntity, entity, i);
        };
        if (livingEntity != null) {
            visor$runIterationOnInventory(enchantmentVisitor, livingEntity.getAllSlots());
        }

        if (livingEntity instanceof Player) {
            visor$runIterationOnItem(enchantmentVisitor, handItem);
        }

    }
    @Unique
    private void visor$runIterationOnItem(EnchantmentVisitor enchantmentVisitor, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            ListTag listTag = itemStack.getEnchantmentTags();

            for(int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag = listTag.getCompound(i);
                BuiltInRegistries.ENCHANTMENT.getOptional(EnchantmentHelper.getEnchantmentId(compoundTag)).ifPresent((enchantment) -> {
                    enchantmentVisitor.accept(enchantment, EnchantmentHelper.getEnchantmentLevel(compoundTag));
                });
            }

        }
    }
    @Unique
    private void visor$runIterationOnInventory(EnchantmentVisitor enchantmentVisitor, Iterable<ItemStack> iterable) {

        for (ItemStack itemStack : iterable) {
            visor$runIterationOnItem(enchantmentVisitor, itemStack);
        }

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
