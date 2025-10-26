package me.phoenixra.visor;

import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.VisorAPI;

import me.phoenixra.visor.compatibility.sodium.SodiumHelper;
import me.phoenixra.visor.core.client.VisorClientImpl;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MixinConfig implements IMixinConfigPlugin {

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    private final Set<String> appliedModFixes = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!ModLoader.get().isModLoaded(VisorAPI.MOD_ID)) {
            VisorClientImpl.LOGGER.info("Visor failed to load, canceled applying mixin '{}'", mixinClassName);
            return false;
        }

        // only try to apply mod mixins if the target class was found
        if (mixinClassName.startsWith("me.phoenixra.visor.compatibility")) {
            try {
                MixinService.getService().getBytecodeProvider().getClassNode(targetClassName);
            } catch (ClassNotFoundException | IOException e) {
                return false;
            }
            String mod = mixinClassName.split("\\.")[4];
            if (appliedModFixes.add(mod)) {
                VisorClientImpl.LOGGER.info("Visor: applying '{}' compatibility patch", mod);
            }
        }

        if(mixinClassName.contains("NoSodium")
                && SodiumHelper.isLoaded()){
            return false;
        }


        return true;
    }
}
