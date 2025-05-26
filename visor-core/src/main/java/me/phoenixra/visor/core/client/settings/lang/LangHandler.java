package me.phoenixra.visor.core.client.settings.lang;

import net.minecraft.client.resources.language.I18n;

public class LangHandler {


    public static final String YES_KEY = "visor.option.yes";
    public static final String NO_KEY = "visor.option.no";
    public static final String ON_KEY = "options.on";
    public static final String OFF_KEY = "options.off";

    public static String getText(String langKey, Object ... params){
        return I18n.get(langKey, params);
    }
    public static boolean existsLangKey(String langKey){
        return I18n.exists(langKey);
    }


    public static String getYes() {
        return LangHandler.getText(YES_KEY);
    }
    public static String getNo() {
        return LangHandler.getText(NO_KEY);
    }
    public static String getOn() {
        return LangHandler.getText(ON_KEY);
    }
    public static String getOff() {
        return LangHandler.getText(OFF_KEY);
    }


}
