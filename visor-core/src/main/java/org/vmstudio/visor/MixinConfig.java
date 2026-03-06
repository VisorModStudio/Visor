package org.vmstudio.visor;

import org.objectweb.asm.tree.AnnotationNode;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;

import org.vmstudio.visor.compatibility.ClassDependentMixin;
import org.vmstudio.visor.compatibility.FieldDependentMixin;
import org.vmstudio.visor.compatibility.MethodDependentMixin;
import org.vmstudio.visor.compatibility.sodium.SodiumHelper;
import org.vmstudio.visor.core.client.VisorClientImpl;
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

    private static final String CLASS_DEPENDENT_MIXIN =
            "L" + ClassDependentMixin.class.getName().replace(".", "/") + ";";
    private static final String METHOD_DEPENDENT_MIXIN =
            "L" + MethodDependentMixin.class.getName().replace(".", "/") + ";";
    private static final String FIELD_DEPENDENT_MIXIN =
            "L" + FieldDependentMixin.class.getName().replace(".", "/") + ";";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!ModLoader.get().isModLoaded(VisorAPI.MOD_ID)) {
            VisorClientImpl.LOGGER.info("Visor failed to load, canceled applying mixin '{}'", mixinClassName);
            return false;
        }

        // only try to apply mod mixins if the target class was found
        if (mixinClassName.startsWith("org.vmstudio.visor.compatibility")) {
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


        try {
            ClassNode mixinClass = MixinService.getService().getBytecodeProvider().getClassNode(mixinClassName);
            if (mixinClass.visibleAnnotations != null) {
                for (AnnotationNode annotation : mixinClass.visibleAnnotations) {
                    if (annotation.desc.equals(CLASS_DEPENDENT_MIXIN)) {
                        String neededClass = (String) annotation.values.get(1);
                        MixinService.getService().getBytecodeProvider().getClassNode(neededClass);
                    } else if (annotation.desc.equals(METHOD_DEPENDENT_MIXIN)) {
                        String neededMethod = (String) annotation.values.get(1);
                        if (MixinService.getService().getBytecodeProvider()
                                .getClassNode(targetClassName).methods.stream()
                                .noneMatch(m -> neededMethod.equals(m.name)))
                        {
                            return false;
                        }
                    } else if (annotation.desc.equals(FIELD_DEPENDENT_MIXIN)) {
                        String neededField = (String) annotation.values.get(1);
                        if (MixinService.getService().getBytecodeProvider()
                                .getClassNode(targetClassName).fields.stream()
                                .noneMatch(f -> neededField.equals(f.name)))
                        {
                            return false;
                        }
                    }
                }
                return true;
            }
        } catch (ClassNotFoundException | IOException e) {
            VisorClientImpl.LOGGER.info("Visor: skipping mixin '{}'", mixinClassName);
            return false;
        }

        if(mixinClassName.contains("NoSodium")
                && SodiumHelper.isLoaded()){
            return false;
        }


        return true;
    }
}
