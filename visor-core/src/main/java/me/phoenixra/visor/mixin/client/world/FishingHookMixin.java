package me.phoenixra.visor.mixin.client.world;

import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Projectile {

    @Shadow
    private boolean biting;

    @Unique
    private boolean visor$biteHandled = false;


    @Shadow
    public abstract Player getPlayerOwner();

    public FishingHookMixin(EntityType<Projectile> entityType,
                            Level level) {
        super(entityType, level);
    }


    @Inject(at = @At(value = "HEAD"), method = "tick")
    private void visor$hapticFeedback(CallbackInfo ci) {
        if (VisorState.getState().isNotActive()) {
            return;
        }
        Player player = this.getPlayerOwner();
        if(player == null || !player.isLocalPlayer()){
            return;
        }
        HandType hand = player.getMainHandItem()
                .getItem() instanceof FishingRodItem
                ? HandType.MAIN : HandType.OFFHAND;
        if (biting && !visor$biteHandled) {
            // bite
            ClientContext.inputManager.triggerHapticPulse(
                    hand,
                    0.005F,
                    160.0F,
                    0.5f
            );
        }
        visor$biteHandled = biting;
    }
}
