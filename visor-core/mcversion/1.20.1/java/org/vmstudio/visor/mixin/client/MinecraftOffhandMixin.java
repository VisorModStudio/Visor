// #!MC-VERSION:: 1.20.1-26.2
package org.vmstudio.visor.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.vmstudio.visor.api.client.input.HandAction;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.tasks.types.TaskSwing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// two-handed VR
@Mixin(Minecraft.class)
public abstract class MinecraftOffhandMixin {

    // ---- Shadow fields ----
    @Shadow
    public MultiPlayerGameMode gameMode;


    /* ***************** *\
  //--------MIXINS--------\\
    \* ***************** */

    @WrapOperation(method = {"continueAttack", "startAttack"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private void visor$markAttackSwing(LocalPlayer instance, InteractionHand hand, Operation<Void> original) {
        if (VisorState.get().isActive()) {
            ClientContext.handRenderer.setSwingType(HandAction.ATTACK);
            original.call(instance,
                    ClientContext.localPlayer.getActiveHand()
                            .asInteractionHand()
            );
            return;
        }
        original.call(instance, hand);
    }


    @WrapWithCondition(method = "continueAttack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;stopDestroyBlock()V"))
    private boolean visor$keepSwingMining(MultiPlayerGameMode instance) {
        if (VisorState.get().isNotActive()) {
            return true;
        }
        TaskSwing swing = TaskSwing.getInstance();
        return swing == null || !swing.isKeepingVanillaMining();
    }


    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void visor$releaseSwingMiningOnUse(CallbackInfo ci) {
        if (VisorState.get().isNotActive()) {
            return;
        }
        TaskSwing swing = TaskSwing.getInstance();
        if (swing != null && swing.isKeepingVanillaMining()) {
            this.gameMode.stopDestroyBlock();
        }
    }


    // continueAttack reads the hand since 1.21.11: a spear in the main hand stops mining
    @WrapOperation(
            method = {"startAttack", "continueAttack"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack visor$getItemInHand(LocalPlayer instance,
                                        InteractionHand hand,
                                        Operation<ItemStack> original) {
        if (VisorState.get().isActive()) {
            return original.call(instance,
                    ClientContext.localPlayer.getActiveHand()
                            .asInteractionHand()
            );
        }
        return original.call(instance, hand);
    }

    @WrapOperation(
            method = "startUseItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/InteractionHand;values()[Lnet/minecraft/world/InteractionHand;"
            )
    )
    private InteractionHand[] visor$useItemOnlyActive(Operation<InteractionHand[]> original) {
        if (VisorState.get().isActive() && VRServerSettings.isTwoHandedVR()) {
            return new InteractionHand[] {
                    ClientContext.localPlayer.getActiveHand().asInteractionHand()
            };
        }
        return original.call();
    }

    @WrapOperation(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private void visor$markUseSwing(LocalPlayer instance, InteractionHand hand, Operation<Void> original) {
        if (VisorState.get().isActive()) {
            ClientContext.handRenderer.setSwingType(HandAction.USE);
        }
        original.call(instance, hand);
    }
}
