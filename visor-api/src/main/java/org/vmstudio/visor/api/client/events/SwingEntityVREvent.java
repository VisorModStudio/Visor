package org.vmstudio.visor.api.client.events;

import lombok.Getter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.common.eventbus.event.VREventCancelable;

@Getter
@VREventCancelable
public class SwingEntityVREvent extends VREvent {
    private final LocalPlayer player;
    private final HandType hand;
    private final Vec3 handPos;
    private final Vec3 handDir;
    private final Vec3 swingPoint;
    private final float itemLength;
    private final float damageRange;
    private final boolean canSwing;

    public SwingEntityVREvent(@NotNull LocalPlayer player,
                              @NotNull HandType hand,
                              @NotNull Vec3 handPos,
                              @NotNull Vec3 handDir,
                              @NotNull Vec3 swingPoint,
                              float itemLength,
                              float damageRange,
                              boolean canSwing){
        super(VisorAPI.addonManager().getCoreAddon());
        this.player = player;
        this.hand = hand;
        this.handPos = handPos;
        this.handDir = handDir;
        this.swingPoint = swingPoint;
        this.itemLength = itemLength;
        this.damageRange = damageRange;
        this.canSwing = canSwing;
    }

}
