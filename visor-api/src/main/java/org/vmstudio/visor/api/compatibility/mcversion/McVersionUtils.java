package org.vmstudio.visor.api.compatibility.mcversion;

import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;

public class McVersionUtils {
    private McVersionUtils() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    // ------- RESOURCES -------

    public static ResourceLocation newResourceLoc(String namespace,
                                                  String path){
        return new ResourceLocation(namespace, path);
    }
    public static ResourceLocation newResourceLoc(String location){
        return new ResourceLocation(location);
    }


    // ------- TEXT HELPERS -------
    public static String filterText(String text,
                                    boolean allowLineBreaks){
        return SharedConstants.filterText(text, allowLineBreaks);
    }

    public static boolean isAllowedChatCharacter(char character){
        return SharedConstants.isAllowedChatCharacter(character);
    }

}
