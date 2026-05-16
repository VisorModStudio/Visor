package org.vmstudio.visor.api.client.render.decoration.hand;

public enum HandRenderState {
    OFF,
    GUI_HAND,
    WORLD_HAND,
    WORLD_HAND_ITEM_ONLY,
    WORLD_HAND_NO_ITEM;

    public boolean isOff(){
        return this == OFF;
    }
    public boolean isGuiHand(){
        return this == GUI_HAND;
    }
    public boolean isWorldHand(){
        return this == WORLD_HAND || this == WORLD_HAND_NO_ITEM || this == WORLD_HAND_ITEM_ONLY;
    }
    public boolean isWithItem(){
        return this == WORLD_HAND || this == WORLD_HAND_ITEM_ONLY;
    }
    public boolean isWithItemOnly(){
        return this == WORLD_HAND_ITEM_ONLY;
    }
}
