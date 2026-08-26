package org.vmstudio.visor.compatibility;

/**
 * Class looker for mods that renamed their packages between versions
 */
public class CompatClasses {
    private CompatClasses() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * @param names candidates in order
     * @return the first one that loads or null if none of them are present
     */
    public static Class<?> find(String... names) {
        for(String name : names) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException | LinkageError ignored) {
                // wrong candidtate
            }
        }
        return null;
    }

    public static Class<?> require(String... names) throws ClassNotFoundException {
        Class<?> found = find(names);
        if (found == null) {
            throw new ClassNotFoundException(String.join(" / ", names));
        }
        return found;
    }
}
