package me.phoenixra.visor.core.client.settings.options;

import me.phoenixra.visor.core.client.settings.VROptionCategory;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VROptionField {
    String key() default "";
    VROptionWidgetType widgetType() default VROptionWidgetType.EMPTY;
    VROptionCategory category() default VROptionCategory.EMPTY;
}
