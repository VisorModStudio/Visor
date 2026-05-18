package org.vmstudio.visor.api.client.events;

import lombok.Getter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.common.eventbus.event.VREventCancelable;

@Getter
@VREventCancelable
public class SwingBlockVREvent extends VREvent {
    private final LocalPlayer player;
    private final HandType hand;
    private final BlockState blockState;
    private final BlockHitResult blockHit;
    private final Item handItem;
    private final float speed;

    public SwingBlockVREvent(@NotNull LocalPlayer player,
                             @NotNull HandType hand,
                             Item handItem,
                             BlockState blockState,
                             BlockHitResult blockHit,
                             float speed) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.player = player;
        this.hand = hand;
        this.handItem = handItem;
        this.blockState = blockState;
        this.blockHit = blockHit;
        this.speed = speed;
    }
}
