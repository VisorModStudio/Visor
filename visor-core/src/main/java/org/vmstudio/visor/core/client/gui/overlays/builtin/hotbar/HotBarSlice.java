package org.vmstudio.visor.core.client.gui.overlays.builtin.hotbar;

import lombok.Getter;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import net.minecraft.resources.ResourceLocation;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

public enum HotBarSlice {
    CENTER(0, GuiTexture.of(McVersionUtils.newResourceLoc(
            "visor:textures/gui/overlays/hotbar/default.png"
    ))),
    TOP(1, GuiTexture.of(McVersionUtils.newResourceLoc(
            "visor:textures/gui/overlays/hotbar/top.png"
    ))),
    TOP_RIGHT(2, GuiTexture.of(McVersionUtils.newResourceLoc(
            "visor:textures/gui/overlays/hotbar/top_right.png"
    ))),
    RIGHT(3, GuiTexture.of(McVersionUtils.newResourceLoc(
            "visor:textures/gui/overlays/hotbar/right.png"
    ))),
    BOTTOM_RIGHT(4, GuiTexture.of(McVersionUtils.newResourceLoc(
            "visor:textures/gui/overlays/hotbar/bottom_right.png"
    ))),
    BOTTOM(5, GuiTexture.of(McVersionUtils.newResourceLoc(
            "visor:textures/gui/overlays/hotbar/bottom.png"
    ))),
    BOTTOM_LEFT(6, GuiTexture.of(McVersionUtils.newResourceLoc(
            "visor:textures/gui/overlays/hotbar/bottom_left.png"
    ))),
    LEFT(7, GuiTexture.of(McVersionUtils.newResourceLoc(
            "visor:textures/gui/overlays/hotbar/left.png"
    ))),
    TOP_LEFT(8, GuiTexture.of(McVersionUtils.newResourceLoc(
            "visor:textures/gui/overlays/hotbar/top_left.png"
    ))),
    NOT_SELECTED(-1, GuiTexture.of(McVersionUtils.newResourceLoc(
            "visor:textures/gui/overlays/hotbar/default.png"
    )));
    @Getter
    final int slot;
    @Getter
    final GuiTexture background;
    HotBarSlice(int slot, GuiTexture background){
        this.slot = slot;
        this.background = background;
    }

    public static HotBarSlice fromSlot(int slot){
        return slot==-1 ? NOT_SELECTED : values()[slot];
    }
}
