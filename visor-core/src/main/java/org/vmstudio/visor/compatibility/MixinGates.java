package org.vmstudio.visor.compatibility;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.util.List;

/**
 * Reads {@link MixinGate} off a mixin and answers whether its conditions hold
 */
public class MixinGates {
    private static final String GATE_DESC = "L" + MixinGate.class.getName().replace('.', '/') + ";";

    private MixinGates() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean classExists(String name) {
        return readClass(name) != null;
    }

    public static boolean isOpen(String mixinClassName, String targetClassName) {
        ClassNode mixin = readClass(mixinClassName);
        if (mixin == null) {
            return false;
        }
        AnnotationNode gate = findGate(mixin);
        if (gate == null) {
            return true;
        }

        for (String name : arrayValue(gate, "classes")) {
            if (readClass(name) == null) {
                return false;
            }
        }

        List<String> methods = arrayValue(gate, "methods");
        List<String> fields = arrayValue(gate, "fields");
        if (methods.isEmpty() && fields.isEmpty()) {
            return true;
        }

        ClassNode target = readClass(targetClassName);
        if (target == null) {
            return false;
        }
        for (String name : methods) {
            if (!declaresMethod(target, name)) {
                return false;
            }
        }
        for (String name : fields) {
            if (!declaresField(target, name)) {
                return false;
            }
        }
        return true;
    }

    private static AnnotationNode findGate(ClassNode mixin) {
        if (mixin.visibleAnnotations == null) {
            return null;
        }
        for (AnnotationNode annotation : mixin.visibleAnnotations) {
            if (GATE_DESC.equals(annotation.desc)) {
                return annotation;
            }
        }
        return null;
    }

    private static List<String> arrayValue(AnnotationNode annotation, String attribute) {
        List<Object> values = annotation.values;
        if (values == null) {
            return List.of();
        }
        for (int i = 0; i + 1 < values.size(); i += 2) {
            if (!attribute.equals(values.get(i))) {
                continue;
            }
            Object value = values.get(i + 1);
            if (value instanceof List<?> list) {
                return list.stream().map(String::valueOf).toList();
            }
            return value == null ? List.of() : List.of(String.valueOf(value));
        }
        return List.of();
    }

    private static boolean declaresMethod(ClassNode target, String name) {
        if (target.methods == null) {
            return false;
        }
        for (MethodNode method : target.methods) {
            if (name.equals(method.name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean declaresField(ClassNode target, String name) {
        if (target.fields == null) {
            return false;
        }
        for (FieldNode field : target.fields) {
            if (name.equals(field.name)) {
                return true;
            }
        }
        return false;
    }

    private static ClassNode readClass(String name) {
        try {
            return MixinService.getService().getBytecodeProvider().getClassNode(name);
        } catch (ClassNotFoundException | IOException e) {
            return null;
        } catch (Throwable t) {
            // don't crash all mixin startup because of broken class
            return null;
        }
    }
}
