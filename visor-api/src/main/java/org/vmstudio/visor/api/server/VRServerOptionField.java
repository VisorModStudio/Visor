package org.vmstudio.visor.api.server;


import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link VRServerSettings} field as configurable
 * from the client VRSettings (World category).
 */
@ApiStatus.Internal
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VRServerOptionField {
    String key() default "";
}
