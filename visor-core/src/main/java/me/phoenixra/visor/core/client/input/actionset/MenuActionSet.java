package me.phoenixra.visor.core.client.input.actionset;

import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.visor.api.client.input.action.RegisterActionSet;
import me.phoenixra.visor.api.client.input.action.VisorAction;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.input.actions.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterActionSet
public class MenuActionSet extends VisorActionSet {
    public final static String ID = "menu";


    public MenuActionSet(VisorAddon owner) {
        super(owner);
    }

    @Override
    protected List<VisorAction> loadActions() {
        return List.of(
                new ActionLeftMouse(this, HandType.MAIN),
                new ActionRightMouse(this, HandType.MAIN),
                new ActionMiddleMouse(this, HandType.MAIN),
                new ActionScrollMouse(this, HandType.MAIN),
                new ActionLeftMouse(this, HandType.OFFHAND),
                new ActionRightMouse(this, HandType.OFFHAND),
                new ActionMiddleMouse(this, HandType.OFFHAND),
                new ActionScrollMouse(this, HandType.OFFHAND),

                new ActionMenu(this)

        );
    }

    @Override
    protected Map<VRInteractionProfileType, Boolean> loadDefaultKeyModifiersActive() {
        return Map.of();
    }

    @Override
    public boolean canActivate() {
        return MC.screen != null;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
