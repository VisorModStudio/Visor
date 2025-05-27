package me.phoenixra.atumvr.api.input.action;

import org.joml.Vector2f;

//@TODO I don't like it. Rework!
public interface VRActionDataVec2 {

    Vector2f getCurrentState();

    boolean isActive();

    boolean isChanged();

    long getLastChangeTime();


    String getId();
}
