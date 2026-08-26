package org.vmstudio.visor.core.client.utils;

import net.minecraft.client.resources.language.I18n;

public class LangHelper {
    public static final String ON_KEY = "options.on";
    public static final String OFF_KEY = "options.off";

    private LangHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getText(String langKey, Object... params) {
        return I18n.get(langKey, params);
    }

    public static boolean existsLangKey(String langKey) {
        return I18n.exists(langKey);
    }

    public static String getOn() {
        return getText(ON_KEY);
    }

    public static String getOff() {
        return getText(OFF_KEY);
    }
}
