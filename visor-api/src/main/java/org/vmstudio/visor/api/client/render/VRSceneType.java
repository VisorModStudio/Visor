package org.vmstudio.visor.api.client.render;

public enum VRSceneType {
    WORLD,
    MAIN_MENU;

    public boolean isMainMenu(){
        return this == MAIN_MENU;
    }
    public boolean isWorld(){
        return this == WORLD;
    }
}
