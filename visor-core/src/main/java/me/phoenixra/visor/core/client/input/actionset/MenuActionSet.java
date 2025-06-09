package me.phoenixra.visor.core.client.input.actionset;

import me.phoenixra.visor.api.client.input.action.RegisterActionSet;
import me.phoenixra.visor.api.client.input.action.VisorAction;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.input.actionset.actions.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
                new ActionMenu(this),

                new ActionLeftMouse(this),
                new ActionRightMouse(this),
                new ActionMiddleMouse(this),
                new ActionScrollMouse(this)

        );
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
