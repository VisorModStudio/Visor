package org.vmstudio.visor.core.client.player.height;

import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.client.settings.enums.HeightUnits;
import org.vmstudio.visor.core.client.utils.LangHelper;

import java.util.ArrayList;
import java.util.List;


public final class HeightFormat {
    private static final float PIVOT_TO_TOP = 0.21f;
    private static final float INCH = 0.0254f;

    private HeightFormat() {}

    public static float cmToPivot(int cm) {
        return cm / 100f - PIVOT_TO_TOP;
    }

    public static float inchesToPivot(int inches) {
        return inches * INCH - PIVOT_TO_TOP;
    }

    public static int pivotToCm(float pivot) {
        return Math.round((pivot + PIVOT_TO_TOP) * 100f);
    }

    public static int pivotToInches(float pivot) {
        return Math.round((pivot + PIVOT_TO_TOP) / INCH);
    }

    public static String format(float pivot) {
        if (VRClientSettings.getHeightUnits() == HeightUnits.IMPERIAL) {
            int inches = pivotToInches(pivot);
            return LangHelper.getText("visor.height.imperial", inches / 12, inches % 12);
        }
        return LangHelper.getText("visor.height.metric", pivotToCm(pivot));
    }


    public static List<Float> heightEntries(int minCm, int maxCm) {
        List<Float> entries = new ArrayList<>();
        if (VRClientSettings.getHeightUnits() == HeightUnits.IMPERIAL) {
            int minInches = (int) Math.ceil(minCm / 100f / INCH);
            int maxInches = (int) Math.floor(maxCm / 100f / INCH);
            for (int inches = minInches; inches <= maxInches; inches++) {
                entries.add(inchesToPivot(inches));
            }
        } else {
            for (int cm = minCm; cm <= maxCm; cm++) {
                entries.add(cmToPivot(cm));
            }
        }
        return entries;
    }

    public static int nearestIndex(List<Float> entries, float pivot) {
        int best = 0;
        for (int i = 1; i < entries.size(); i++) {
            if (Math.abs(entries.get(i) - pivot) < Math.abs(entries.get(best) - pivot)) {
                best = i;
            }
        }
        return best;
    }
}
