package me.phoenixra.atumvr.api.input.action;


public interface VRActionDataButton {

    boolean isActive();

    boolean isPressed();

    boolean isButtonChanged();

    long getButtonLastChangeTime();

    String getId();
}
