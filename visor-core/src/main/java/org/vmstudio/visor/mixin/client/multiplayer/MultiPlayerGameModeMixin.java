package org.vmstudio.visor.mixin.client.multiplayer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.compatibility.ItemClassifier;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import org.vmstudio.visor.core.client.network.ClientNetworking;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Shadow
    private ItemStack destroyingItem;



    @Shadow
    public abstract void startPrediction(ClientLevel arg, PredictiveAction arg2);


    /* ***************************************** *\
  //--------TWO HANDED VR (OFFHAND SUPPORT)--------\\
    \* ***************************************** */

    @Redirect(method = "sameDestroyTarget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack visor$sameDestroyTarget(LocalPlayer player) {
        return visor$getUsedItem(player);
    }

    @Redirect(
            method = "startDestroyBlock", // Target the synthetic lambda method
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V"
            )
    )
    public void visor$startDestroyBlock(MultiPlayerGameMode instance,
                                        ClientLevel clientLevel,
                                        PredictiveAction predictiveAction
    ) {
        if(VisorState.get().isNotActive()) {
            instance.startPrediction(clientLevel,predictiveAction);
            return;
        }
        startPrediction(clientLevel, (i) -> {
            Packet<ServerGamePacketListener> packet = predictiveAction.predict(i);
            destroyingItem = visor$getUsedItem(MC.player);
            return packet;
        });
    }

    @Redirect(method = "destroyBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack visor$destroyBlock(LocalPlayer player) {
        return visor$getUsedItem(player);
    }



    /* ************************** *\
  //--------PLAYER LOOK POSE--------\\
    \* ************************** */
    @Inject(at = @At("HEAD"), method = "useItem")
    public void visor$ensureUseDirection1(Player player,
                                            InteractionHand interactionHand,
                                            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (VisorState.get().isActive()) {
            ClientNetworking.sendLookPacket(player,
                    visor$getRightClickLook(
                                    player,
                                    interactionHand == InteractionHand
                                            .MAIN_HAND ?
                                            HandType.MAIN
                                            : HandType.OFFHAND
                            )
            );
        }
    }

    @Inject(at = @At("HEAD"), method = "releaseUsingItem")
    public void visor$ensureUseDirection2(Player player, CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ClientNetworking.sendLookPacket(player,
                    visor$getRightClickLook(
                                    player,
                                    player.getUsedItemHand() == InteractionHand
                                            .MAIN_HAND ?
                                            HandType.MAIN
                                            : HandType.OFFHAND
                            )
            );
        }
    }

    @Inject(at = @At("HEAD"), method = "useItemOn")
    public void visor$ensureUseDirection3(LocalPlayer localPlayer, InteractionHand interactionHand,
                                   BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (VisorState.get().isActive()) {
            ClientNetworking.sendLookPacket(
                    localPlayer,
                    blockHitResult.getLocation()
                            .subtract(
                                    localPlayer.getEyePosition(1.0F)
                            ).normalize()
            );
        }
    }


    /* ************************* *\
  //--------UTILITY METHODS--------\\
    \* ************************* */

    @Unique
    public ItemStack visor$getUsedItem(Player player) {
        if(VisorState.get().isNotActive()) return player.getMainHandItem();
        if (VRServerSettings.isTwoHandedVR()
                && ClientContext.localPlayer.getActiveHand() == HandType.OFFHAND) {
            return player.getOffhandItem();
        }
        return player.getMainHandItem();
    }

    @Unique
    public Vec3 visor$getRightClickLook(Player player,
                                       HandType handType) {
        // Start with the player's default look direction.
        Vec3 lookDirection = player.getLookAngle();

        // If a custom cross vector is available, adjust the look direction accordingly.
        GameRendererExtension renderer = (GameRendererExtension) MC.gameRenderer;
        Vec3 crossVector = renderer.visor$getCrossVec();
        if (crossVector != null) {
            lookDirection = player.getEyePosition(1.0F)
                    .subtract(crossVector)
                    .normalize()
                    .reverse();
        }

        // Get the item held in the specified hand.
        ItemStack heldItem = (handType == HandType.MAIN)
                ? player.getMainHandItem()
                : player.getOffhandItem();

        // Check if the held item qualifies for aim adjustments.
        boolean isThrowable = ItemClassifier.THROWABLE.is(heldItem.getItem());
        boolean isPotion = heldItem.getItem() instanceof PotionItem;
        boolean isBow = heldItem.getItem() instanceof BowItem;
        boolean isChargedCrossbow = heldItem.getItem() instanceof CrossbowItem
                && CrossbowItem.isCharged(heldItem);

        // If the held item affects aiming, update the look direction.
        if (isThrowable || isPotion || isBow || isChargedCrossbow) {
            VRPlayerPoseClient preTickPose = ClientContext
                    .localPlayer.getPoseData(PlayerPoseType.TICK);
            lookDirection = new Vec3(
                    (Vector3f) preTickPose.getHand(handType).getDirection()
            );


        }

        return lookDirection;
    }
}
