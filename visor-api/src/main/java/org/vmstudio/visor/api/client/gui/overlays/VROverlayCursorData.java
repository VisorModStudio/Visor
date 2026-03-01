package org.vmstudio.visor.api.client.gui.overlays;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VROverlayCursorData {

    /**
     * Cursor X position relative to overlay coordinates
     */
    private int cursorX;

    /**
     * Cursor Y position relative to overlay coordinates
     */
    private int cursorY;

    /**
     * Raw cursor X position<br>
     *
     * <p>If Value is from 0 to 1,
     * then cursor is within overlay bounds</p>
     */
    private float rawCursorX;

    /**
     * Raw cursor Y position<br>
     *
     * <p>If Value is from 0 to 1,
     * then cursor is within overlay bounds</p>
     *
     */
    private float rawCursorY;


    /**
     * Is cursor within GUI bounds
     *
     * @return true/false
     */
    public boolean isInGui(){

        return rawCursorX >= 0f && rawCursorX <= 1f
                && rawCursorY >= 0f && rawCursorY <= 1f;
    }

}
