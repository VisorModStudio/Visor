package me.phoenixra.visor.core.client.settings.option;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VROptionField {
    String key() default "";
    VRGuiOption guiOptionType() default VRGuiOption.NONE;
}
