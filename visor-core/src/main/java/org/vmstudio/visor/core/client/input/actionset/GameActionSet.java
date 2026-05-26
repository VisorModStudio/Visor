package org.vmstudio.visor.core.client.input.actionset;

import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import org.vmstudio.visor.api.client.input.action.RegisterActionSet;
import org.vmstudio.visor.api.client.input.action.VRAction;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.VRActions;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.core.client.input.actions.*;
import org.vmstudio.visor.core.client.input.actions.game.*;

import java.util.List;
import java.util.Map;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@RegisterActionSet
public class GameActionSet extends VRActionSet {
    public final static String ID = "game";


    public GameActionSet(VisorAddon owner) {
        super(owner);
    }

    @Override
    protected List<VRAction> loadActions() {
        return List.of(
                VRActions.createMouseLeft(this, HandType.MAIN),
                VRActions.createMouseRight(this, HandType.MAIN),
                VRActions.createMouseMiddle(this, HandType.MAIN),
                VRActions.createMouseScroll(this, HandType.MAIN),

                VRActions.createMouseLeft(this, HandType.OFFHAND),
                VRActions.createMouseRight(this, HandType.OFFHAND),
                VRActions.createMouseMiddle(this, HandType.OFFHAND),
                VRActions.createMouseScroll(this, HandType.OFFHAND),

                VRActions.createShift(this),
                VRActions.createMenu(this),

                new GameActionMovement(this),
                new GameActionRotate(this),
                new GameActionJump(this),

                new GameActionHotBar(this, HandType.MAIN),
                new GameActionHotBar(this, HandType.OFFHAND)
        );
    }

    @Override
    public Map<VRInteractionProfileType, Boolean> getDefaultKeyModifiersActive() {
        return Map.of();
    }

    @Override
    public boolean canActivate() {
        return MC.screen == null && MC.player != null;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
