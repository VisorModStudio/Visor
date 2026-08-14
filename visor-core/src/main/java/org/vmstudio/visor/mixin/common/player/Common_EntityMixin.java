package org.vmstudio.visor.mixin.common.player;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class Common_EntityMixin {

    @Shadow @Final protected RandomSource random;

    @Shadow
    public abstract Pose getPose();

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract boolean onGround();


    @Shadow
    public abstract double getZ();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract Vec3 position();

    @Shadow public abstract float getXRot();

    @Shadow public abstract float getYRot();

    @Shadow public abstract double getEyeY();

    @Shadow public abstract void setDeltaMovement(double x, double y, double z);

    @Shadow public abstract Vec3 getDeltaMovement();

    @Shadow public abstract boolean isPassenger();

    @Shadow public abstract boolean isSwimming();

    @Shadow protected abstract float getBlockJumpFactor();

    @Shadow public abstract boolean isSilent();

    @Shadow protected Vec3 stuckSpeedMultiplier;

    @Shadow public abstract void setOnGround(boolean onGround);

    @Inject(method = "setPosRaw", at = @At("TAIL"))
    protected void visor$injectSetPosRaw(double x,
                                         double y,
                                         double z,
                                         CallbackInfo ci){

    }
    @Inject(method = "setPos(DDD)V", at = @At("TAIL"))
    protected void visor$injectSetPos(double x,
                                      double y,
                                      double z,
                                      CallbackInfo ci){

    }

    @WrapMethod(method = "moveRelative")
    protected void visor$wrapMoveRelative(float amount,
                                          Vec3 relative,
                                          Operation<Void> original){
        original.call(amount, relative);
    }

    @WrapMethod(method = "move")
    protected void visor$wrapMove(MoverType type,
                                  Vec3 pos,
                                  Operation<Void> original){
        original.call(type, pos);
    }


    @Unique
    private static final AttributeModifier VISOR$WALK_UP_BLOCKS = new AttributeModifier(
            ResourceLocation.fromNamespaceAndPath("visor", "walk_up_blocks"),
            0.4D, AttributeModifier.Operation.ADD_VALUE
    );

    /**
     * 1.21.1: Entity#setMaxUpStep is gone, step height is the
     * STEP_HEIGHT attribute now (base 0.6). Heights above the
     * default apply a transient +0.4 modifier instead.
     */
    @Unique
    protected void visor$setMaxUpStep(float height) {
        if (!((Object) this instanceof LivingEntity living)) {
            return;
        }
        AttributeInstance attribute = living.getAttribute(Attributes.STEP_HEIGHT);
        if (attribute == null) {
            return;
        }
        if (height > 0.6F) {
            if (!attribute.hasModifier(VISOR$WALK_UP_BLOCKS.id())) {
                attribute.addTransientModifier(VISOR$WALK_UP_BLOCKS);
            }
        } else {
            attribute.removeModifier(VISOR$WALK_UP_BLOCKS.id());
        }
    }

}
