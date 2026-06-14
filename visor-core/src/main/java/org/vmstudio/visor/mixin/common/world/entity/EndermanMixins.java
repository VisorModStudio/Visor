package org.vmstudio.visor.mixin.common.world.entity;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class EndermanMixins {


    @Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanFreezeWhenLookedAt")
    public static class EndermanFreezeWhenLookedAtMixin {

        @Shadow
        @Nullable
        private LivingEntity target;

        @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/control/LookControl;setLookAt(DDD)V"),
                method = "tick")
        public void visor$lookAtHmd(LookControl instance,
                                    double d, double e, double f) {
            if (this.target instanceof ServerPlayer player) {
                VRServerPlayer vrPlayer = VisorAPI.server()
                        .getVRPlayer(
                                player
                        );
                if (vrPlayer != null) {
                    instance.setLookAt(
                            vrPlayer.getPoseData().getHmd()
                                    .getPositionVec3()
                    );
                    return;
                }
            }
            instance.setLookAt(d,e,f);
        }
    }



    @Mixin(EnderMan.class)
    public abstract static class EndermanMixin extends Monster {

        protected EndermanMixin(EntityType<? extends Monster> entityType, Level level) {
            super(entityType, level);
        }

        @Inject(at = @At("HEAD"), method = "isLookingAtMe(Lnet/minecraft/world/entity/player/Player;)Z", cancellable = true)
        public void visor$vrPlayerLookingAtMe(Player player, CallbackInfoReturnable<Boolean> cir) {
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            var vrPlayer = VisorAPI.server().getVRPlayer(serverPlayer);
            if (vrPlayer != null) {
                cir.setReturnValue(
                        visor$canAttackVrPlayer(
                                (EnderMan) (Object) this,
                                serverPlayer
                        )
                );
            }
        }

        @Unique
        private static boolean visor$canAttackVrPlayer(EnderMan enderman,
                                                       ServerPlayer player) {
            ItemStack itemstack = player.getInventory().armor.get(3);
            if (!itemstack.is(Items.CARVED_PUMPKIN)) { //no ender item
                if (player.level() != enderman.level()) return false;
                VRServerPlayer vrPlayer = VisorAPI.server()
                        .getVRPlayer(player);
                if (vrPlayer == null) return false;

                var hmd = vrPlayer.getPoseData().getHmd();

                Vec3 hmdPos = hmd.getPositionVec3();
                Vec3 relativePos = new Vec3(
                        enderman.getX() - hmdPos.x,
                        enderman.getEyeY() - hmdPos.y,
                        enderman.getZ() - hmdPos.z
                );

                double relativeLength = relativePos.length();

                relativePos = relativePos.normalize();
                double dotProd = hmd.getDirectionVec3()
                        .dot(relativePos);

                return dotProd > 0.975
                        && relativeLength < 128.0
                        &&
                        visor$canEntityBeSeen(
                                enderman,
                                hmdPos
                        );
            }

            return false;
        }

        @Unique
        private static boolean visor$canEntityBeSeen(Entity entity,
                                                     Vec3 playerEyePos) {
            Vec3 entityEyePos = new Vec3(
                    entity.getX(),
                    entity.getEyeY(),
                    entity.getZ()
            );
            return entity.level().clip(
                    new ClipContext(
                            playerEyePos,
                            entityEyePos,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            entity
                    )
            ).getType() == HitResult.Type.MISS;
        }
    }
}
