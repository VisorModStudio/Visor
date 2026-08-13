package org.vmstudio.visor.api.compatibility.mcversion;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;


public class McVersionUtils {
    private McVersionUtils() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }
    public static ResourceLocation newResourceLoc(String namespace,
                                                  String path){
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
    public static ResourceLocation newResourceLoc(String location){
        return ResourceLocation.parse(location);
    }

    //---------- chat text helpers (moved from SharedConstants to StringUtil in 1.20.3) ----------

    public static String filterText(String text, boolean allowLineBreaks){
        return StringUtil.filterText(text, allowLineBreaks);
    }

    public static boolean isAllowedChatCharacter(char character){
        return StringUtil.isAllowedChatCharacter(character);
    }

}
