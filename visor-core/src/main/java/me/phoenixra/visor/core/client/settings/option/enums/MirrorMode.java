package me.phoenixra.visor.core.client.settings.option.enums;

public enum MirrorMode {
    OFF,
    GUI,
    CROPPED_LEFT,
    CROPPED_RIGHT,
    SINGLE_LEFT,
    SINGLE_RIGHT,
    DUAL,
    FIRST_PERSON,
    THIRD_PERSON;


    public boolean isCropped(){
        return this == CROPPED_LEFT || this == CROPPED_RIGHT;
    }

    public boolean isSingle(){
        return this == SINGLE_LEFT || this == SINGLE_RIGHT;
    }



}
