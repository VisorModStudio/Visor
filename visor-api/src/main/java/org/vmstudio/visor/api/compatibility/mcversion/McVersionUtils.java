package org.vmstudio.visor.api.compatibility.mcversion;

import net.minecraft.resources.ResourceLocation;

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
}
