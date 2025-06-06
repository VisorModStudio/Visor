package me.phoenixra.visor.core.client.input.actionset;

import me.phoenixra.visor.api.client.input.action.RegisterActionSet;
import me.phoenixra.visor.api.client.input.action.VisorAction;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.common.addon.VisorAddon;
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
