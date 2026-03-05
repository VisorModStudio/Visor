package org.vmstudio.visor.mixin.client.multiplayer;

import org.vmstudio.visor.api.client.player.pose.PlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
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
            PlayerPoseClient preTickPose = ClientContext
                    .localPlayer.getPoseData(PlayerPoseType.TICK);
            lookDirection = new Vec3(
                    (Vector3f) preTickPose.getHand(handType).getDirection()
            );


        }

        return lookDirection;
    }
}
