package me.phoenixra.atumvr.api.input.action;

import org.joml.Vector2f;


public interface VRActionDataVec2 {

    Vector2f getCurrentState();

    boolean isActive();

    boolean isChanged();

    long getLastChangeTime();


    String getId();
}
