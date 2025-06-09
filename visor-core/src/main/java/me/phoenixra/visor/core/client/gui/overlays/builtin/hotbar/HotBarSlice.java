package me.phoenixra.visor.core.client.gui.overlays.builtin.hotbar;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

public enum HotBarSlice {
    CENTER(0, new ResourceLocation(
            "visor:textures/gui/hotbar/default.png"
    )),
    TOP(1,new ResourceLocation(
            "visor:textures/gui/hotbar/top.png"
    )),
    TOP_RIGHT(2,new ResourceLocation(
            "visor:textures/gui/hotbar/top_right.png"
    )),
    RIGHT(3,new ResourceLocation(
            "visor:textures/gui/hotbar/right.png"
    )),
    BOTTOM_RIGHT(4,new ResourceLocation(
            "visor:textures/gui/hotbar/bottom_right.png"
    )),
    BOTTOM(5,new ResourceLocation(
            "visor:textures/gui/hotbar/bottom.png"
    )),
    BOTTOM_LEFT(6,new ResourceLocation(
            "visor:textures/gui/hotbar/bottom_left.png"
    )),
    LEFT(7,new ResourceLocation(
            "visor:textures/gui/hotbar/left.png"
    )),
    TOP_LEFT(8,new ResourceLocation(
            "visor:textures/gui/hotbar/top_left.png"
    )),
    NOT_SELECTED(-1,new ResourceLocation(
            "visor:textures/gui/hotbar/default.png"
    ));
    @Getter
    final int slot;
    @Getter
    final ResourceLocation image;
    HotBarSlice(int slot, ResourceLocation hotbarImage){
        this.slot = slot;
        this.image = hotbarImage;
    }

    public static HotBarSlice fromSlot(int slot){
        return slot==-1 ? NOT_SELECTED : values()[slot];
    }
}
