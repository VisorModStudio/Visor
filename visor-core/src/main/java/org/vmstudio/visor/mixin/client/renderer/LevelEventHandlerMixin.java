package org.vmstudio.visor.mixin.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;

// 1.21.4: LevelRenderer#levelEvent moved into the new LevelEventHandler
@Mixin(LevelEventHandler.class)
public abstract class LevelEventHandlerMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(at = @At("HEAD"), method = "levelEvent")
    public void visor$hapticOnSound(int i, BlockPos blockPos, int j, CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;

        if (this.minecraft.player != null
                && this.minecraft.player.isAlive()
                && this.minecraft.player.blockPosition().distSqr(blockPos) < 25.0D) {
            switch (i) {
                case 1019,      // ZOMBIE_ATTACK_WOODEN_DOOR
                     1020,   // ZOMBIE_ATTACK_IRON_DOOR
                     1021    // ZOMBIE_BREAK_WOODEN_DOOR
                        -> {
                    ClientContext.inputManager
                            .triggerHapticPulse(HandType.MAIN, 0.0075f);
                    ClientContext.inputManager
                            .triggerHapticPulse(HandType.OFFHAND, 0.0075f);
                }
                case 1030 ->    // ANVIL_USE
                        ClientContext.inputManager
                                .triggerHapticPulse(HandType.MAIN, 0.005f);
                case 1031 -> {  // ANVIL_LAND
                    ClientContext.inputManager
                            .triggerHapticPulse(HandType.MAIN, 0.0125f);
                    ClientContext.inputManager
                            .triggerHapticPulse(HandType.OFFHAND, 0.0125f);
                }
            }
        }
    }
}
