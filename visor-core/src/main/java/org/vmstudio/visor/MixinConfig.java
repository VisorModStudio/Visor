package org.vmstudio.visor;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.vmstudio.visor.compatibility.MixinGates;

import java.util.List;
import java.util.Set;

public class MixinConfig implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger(MixinModLoader.MOD_NAME);
    private static final String COMPAT_PACKAGE = "org.vmstudio.visor.compatibility.";
    private static final String SODIUM_EXCLUSIVE_MARKER = "NoSodium";
    private final Set<String> loggedCompatTargets = Sets.newConcurrentHashSet();

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!MixinModLoader.get().isModLoaded(MixinModLoader.MOD_ID)) {
            LOGGER.info("Visor failed to load, canceled applying mixin '{}'", mixinClassName);
            return false;
        }

        if (mixinClassName.contains(SODIUM_EXCLUSIVE_MARKER) && MixinModLoader.get().isSodiumLoaded()) {
            return false;
        }

        if (!MixinGates.isOpen(mixinClassName, targetClassName)) {
            LOGGER.debug("Visor: gate closed for mixin '{}', skipping", mixinClassName);
            return false;
        }

        if (mixinClassName.startsWith(COMPAT_PACKAGE)) {
            if (!MixinGates.classExists(targetClassName)) {
                return false;
            }
            logCompatTarget(mixinClassName);
        }

        return true;
    }

    // instead of adding loggers for every compatibility
    private void logCompatTarget(String mixinClassName) {
        String tail = mixinClassName.substring(COMPAT_PACKAGE.length());
        int dot = tail.indexOf('.');
        String mod = dot < 0 ? tail : tail.substring(0, dot);
        if (loggedCompatTargets.add(mod)) {
            LOGGER.info("Visor: applying '{}' compatibility patch", mod);
        }
    }
}
