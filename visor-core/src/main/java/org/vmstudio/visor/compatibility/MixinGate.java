package org.vmstudio.visor.compatibility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MixinGate {
    /**
     * fully qualified names that must all be loadable
     */
    String[] classes() default {};

    /**
     * method names the mixin's own
     */
    String[] methods() default {};

    /**
     * field names the mixin's own
     */
    String[] fields() default {};
}
