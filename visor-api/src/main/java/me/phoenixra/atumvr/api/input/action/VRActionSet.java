package me.phoenixra.atumvr.api.input.action;

import me.phoenixra.atumvr.core.input.action.OpenXRAction;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

public interface VRActionSet {


    void init();

    void update(@Nullable Consumer<String> listener);

    void destroy();




    Collection<? extends VRAction> getActions();


    String getName();

    String getLocalizedName();

    int getPriority();
}
