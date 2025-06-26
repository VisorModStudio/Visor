package me.phoenixra.visor.api.client.gui.overlay.framework;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VROverlayCursorData {

    /**
     * Cursor X position relative to overlay coordinates
     */
    protected int cursorX;

    /**
     * Cursor Y position relative to overlay coordinates
     */
    protected int cursorY;

    /**
     * Raw cursor X position<br>
     *
     * <p>If Value is from 0 to 1,
     * then cursor is within overlay bounds</p>
     */
    protected float rawCursorX;

    /**
     * Raw cursor Y position<br>
     *
     * <p>If Value is from 0 to 1,
     * then cursor is within overlay bounds</p>
     *
     */
    protected float rawCursorY;



    public boolean isInGui(){

        return rawCursorX >= 0f && rawCursorX <= 1f
                && rawCursorY >= 0f && rawCursorY <= 1f;
    }

}
