package org.vmstudio.visor.api.client.gui.helpers;

import lombok.Getter;
@Getter
public class ScaleHelper {

    private final float originWidth;
    private final float originHeight;

    private float scale;
    private int startX;
    private int startY;

    public ScaleHelper(float originWidth,
                       float originHeight){
        this.originWidth = originWidth;
        this.originHeight = originHeight;
    }

    /**
     * Call this in init() to compute the uniform scale
     * and the offset to center the content on screen.
     */
    public void computeScale(float screenWidth, float screenHeight) {
        scale = Math.min(screenWidth / originWidth, screenHeight / originHeight);
        startX = (int) ((screenWidth - originWidth * scale) / 2f);
        startY = (int) ((screenHeight - originHeight * scale) / 2f);
    }

    /** Origin-space X → screen-space X */
    public int scaledX(float originX) {
        return startX + Math.round(originX * scale);
    }

    /** Origin-space Y → screen-space Y */
    public int scaledY(float originY) {
        return startY + Math.round(originY * scale);
    }

    /** Scale a Origin-space size (width or height) → screen-space */
    public int scaledSize(float originSize) {
        return Math.round(originSize * scale);
    }
}
