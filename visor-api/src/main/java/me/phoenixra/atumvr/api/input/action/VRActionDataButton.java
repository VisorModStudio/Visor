package me.phoenixra.atumvr.api.input.action;

//@TODO I don't like it. Rework!
public interface VRActionDataButton {

    boolean isActive();

    boolean isPressed();

    boolean isButtonChanged();

    long getButtonLastChangeTime();

    String getId();
}
