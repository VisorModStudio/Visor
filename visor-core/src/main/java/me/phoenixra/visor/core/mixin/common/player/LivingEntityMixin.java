package me.phoenixra.visor.core.mixin.common.player;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.server.player.VRServerPlayer;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level
    ) {
        super(entityType, level);
    }

    @Inject(at = @At("HEAD"), method = "spawnItemParticles", cancellable = true)
    private void visor$spawnVRItemParticles(ItemStack itemStack,
                                           int count,
                                           CallbackInfo ci){
        LivingEntity instance = (LivingEntity) (Object)this;
        if(!(instance instanceof ServerPlayer player)){
            return;
        }
        ci.cancel();

        VRServerPlayer serverPlayer = VisorAPI.server().getVrPlayer(player);
        for (int i = 0; i < count; ++i) {
            Vec3 velocity = new Vec3(
                    ((double) this.random.nextFloat() - 0.5D) * 0.1D,
                    Math.random() * 0.1D + 0.1D,
                    0.0D
            );
            velocity = velocity.xRot(
                    -this.getXRot() * ((float) Math.PI / 180F)
            );
            velocity = velocity.yRot(
                    -this.getYRot() * ((float) Math.PI / 180F)
            );
            double verticalOffset = (double) (-this.random.nextFloat()) * 0.6D - 0.3D;
            Vec3 particlePos;
            if (serverPlayer != null && serverPlayer.isVr()) {
                InteractionHand interactionhand = player.getUsedItemHand();

                if (interactionhand == InteractionHand.MAIN_HAND) {
                    particlePos = serverPlayer.getControllerPos(
                            ControllerHand.MAIN
                    );
                } else {
                    particlePos = serverPlayer.getControllerPos(
                            ControllerHand.OFFHAND
                    );
                }
            }else{
                particlePos = new Vec3(
                        ((double) this.random.nextFloat() - 0.5D) * 0.3D,
                        verticalOffset,
                        0.6D
                );
                particlePos = particlePos.xRot(
                        -this.getXRot() * ((float) Math.PI / 180F)
                );
                particlePos = particlePos.yRot(
                        -this.getYRot() * ((float) Math.PI / 180F)
                );
                particlePos = particlePos.add(
                        this.getX(),
                        this.getEyeY(),
                        this.getZ()
                );
            }
            //to not have an annoying particles displaying
            //too close to player eyes
            particlePos = particlePos
                    .add(0,-0.8,0);
            if (this.level() instanceof ServerLevel) {
                ((ServerLevel)this.level()).sendParticles(
                        new ItemParticleOption(
                                ParticleTypes.ITEM,
                                itemStack
                        ),
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        1, velocity.x,
                        velocity.y + 0.05D,
                        velocity.z,
                        0.0
                );
            }else {
                this.level().addParticle(
                        new ItemParticleOption(
                                ParticleTypes.ITEM,
                                itemStack
                        ),
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        velocity.x,
                        velocity.y + 0.05D,
                        velocity.z
                );
            }
        }
    }

}
