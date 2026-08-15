package org.vmstudio.visor;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.ModLoader;


/**
 * Interface for early mixin initialization, separate from ModLoader,
 * to not load any minecraft and other unnecessary classes.
 */
public interface MixinModLoader {
    String MOD_ID = "visor";
    String MOD_NAME = "Visor";


    boolean isModLoaded(@NotNull String id);

    default boolean isSodiumLoaded() {
        return ModLoader.get().isModLoaded("sodium")
                || ModLoader.get().isModLoaded("rubidium")
                || ModLoader.get().isModLoaded("embeddium");
    }

    @NotNull
    LoaderType getType();


    static MixinModLoader get() {
        return MixinModLoader.Instance.get();
    }


    enum LoaderType{
        FABRIC,
        FORGE,
        NEOFORGE
    }


    @ApiStatus.Internal
    final class Instance {
        private Instance() {
            throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
        }

        private static MixinModLoader api;

        static MixinModLoader get() {
            if(api != null){
                return api;
            }

            //FORGE
            try {
                Class<?> clazz = Class.forName("org.vmstudio.visor.loader.forge.ForgeMixinModLoader");
                api = (MixinModLoader) clazz.getConstructor().newInstance();
            } catch (Exception ignored) {
            }
            //NEOFORGE
            if(api == null){
                try {
                    Class<?> clazz = Class.forName("org.vmstudio.visor.loader.neoforge.NeoForgeMixinModLoader");
                    api = (MixinModLoader) clazz.getConstructor().newInstance();
                } catch (Exception ignored) {
                }
            }
            //FABRIC
            if(api == null){
                try {
                    Class<?> clazz = Class.forName("org.vmstudio.visor.loader.fabric.FabricMixinModLoader");
                    api = (MixinModLoader) clazz.getConstructor().newInstance();
                } catch (Exception ignored) {
                }
            }

            if(api == null){
                throw new RuntimeException("SUPPORTED MIXIN MOD LOADER FOR" +
                        " VISOR NOT FOUND!");
            }
            return api;
        }
    }
}
