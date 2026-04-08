package org.vmstudio.visor.mixin.common.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.api.server.player.VRServerPlayer;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    /* ************************* *\
  //--------OFFHAND SUPPORT--------\\
    \* ************************* */
    @Redirect(method = "destroyBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack visor$destroyBlock(ServerPlayer player) {
        if(!VRServerSettings.isOffhandUsable()) return player.getMainHandItem();
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(player);
        if (vrPlayer == null) {
            return player.getMainHandItem();
        }
        if (vrPlayer.getActiveHand() == HandType.OFFHAND) {
            return player.getOffhandItem();
        } else {
            return player.getMainHandItem();
        }
    }
}
