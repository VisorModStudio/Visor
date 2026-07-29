package org.vmstudio.visor.api.client.settings;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ApiStatus.Internal
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VROptionField {
    String key() default "";
    VROptionCategory category() default VROptionCategory.EMPTY;

    boolean excludeForcedChange() default false;
}
