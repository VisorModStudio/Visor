package org.vmstudio.visor.api.server;


import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@ApiStatus.Internal
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SendSettingToClient {
}
