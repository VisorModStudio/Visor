package me.phoenixra.atumvr.api.input.action;

import java.util.Collection;

public interface VRActionSet {


    void init();

    void update();

    void destroy();




    Collection<? extends VRAction> getActions();


    String getName();

    String getLocalizedName();

    int getPriority();
}
