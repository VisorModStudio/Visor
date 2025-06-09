package me.phoenixra.visor.api.client.gui.overlay.types;

import lombok.Getter;

@Getter
public class OverlayCursorData {

    public int mouseX;
    public int mouseY;

    public float rawCursorX;
    public float rawCursorY;

    public float cursorInGuiX;
    public float cursorInGuiY;


    public boolean isInGui(){
        return rawCursorX != -1 && rawCursorY != -1;
    }

}
