package org.vmstudio.visor.mixin.common.player;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.core.common.player.OffhandNonNullList;

import java.util.Arrays;
import java.util.List;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements Container, Nameable {
    @Final
    @Shadow
    public NonNullList<ItemStack> items;
    @Final
    @Shadow
    public NonNullList<ItemStack> armor;
    @Final
    @Shadow
    @Mutable
    public NonNullList<ItemStack> offhand;
    @Final
    @Shadow
    @Mutable
    private List<NonNullList<ItemStack>> compartments;

    @Shadow
    public int selected;

    @Shadow
    @Final
    public Player player;


    /* ***************************************** *\
  //--------TWO HANDED VR (OFFHAND SUPPORT)--------\\
    \* ***************************************** */

    /**
     * Replaces vanilla offhand list with a custom one,
     * that uses similar logic as main hand
     */
    @Inject(at = @At("TAIL"), method = "<init>")
    public void visor$replaceOffhandList(Player player, CallbackInfo ci) {
        offhand = visor$createOffhandList(1, ItemStack.EMPTY);
        this.compartments = ImmutableList.of(
                this.items, this.armor, this.offhand
        );

    }


    //LOAD
    @Inject( method = "load", at = @At(value = "HEAD"))
    public void visor$loadHead(ListTag listTag, CallbackInfo ci) {
        var offhand = (OffhandNonNullList) this.offhand;
        offhand.setUseVanilla(true);
    }
    @Inject( method = "load", at = @At(value = "TAIL"))
    public void visor$loadTail(ListTag listTag, CallbackInfo ci) {
        var offhand = (OffhandNonNullList) this.offhand;
        offhand.setUseVanilla(false);
    }
    //SAVE
    @Inject( method = "save", at = @At(value = "HEAD"))
    public void visor$saveHead(ListTag listTag, CallbackInfoReturnable<ListTag> cir) {
        var offhand = (OffhandNonNullList) this.offhand;
        offhand.setUseVanilla(true);
    }
    @Inject( method = "save", at = @At(value = "TAIL"))
    public void visor$saveTail(ListTag listTag, CallbackInfoReturnable<ListTag> cir) {
        var offhand = (OffhandNonNullList) this.offhand;
        offhand.setUseVanilla(false);
    }


    @Inject(at = @At("HEAD"), method = "getDestroySpeed", cancellable = true)
    public void visor$offhandDestroySpeed(BlockState blockState,
                                          CallbackInfoReturnable<Float> ci
    ) {
        if (!VRServerSettings.isTwoHandedVR()) {
            return;
        }
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(player);
        if (vrPlayer == null) return;

        if (vrPlayer.getActiveHand() == HandType.MAIN) {
            ci.setReturnValue(
                    this.items.get(this.selected).getDestroySpeed(blockState)
            );
        } else {
            ci.setReturnValue(
                    player.getOffhandItem()
                            .getDestroySpeed(blockState)
            );
        }

    }

    @Unique
    public OffhandNonNullList visor$createOffhandList(int i, ItemStack object) {
        Validate.notNull(object);
        ItemStack[] objects = new ItemStack[i];
        Arrays.fill(objects, object);
        return new OffhandNonNullList(
                player,
                Arrays.asList(objects),
                object
        );
    }
}
