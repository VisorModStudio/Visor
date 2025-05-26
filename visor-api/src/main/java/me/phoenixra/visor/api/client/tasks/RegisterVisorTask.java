package me.phoenixra.visor.api.client.tasks;



import me.phoenixra.visor.api.common.addon.VisorAddon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to register your Visor task
 * <br><br>
 * Class have to:<br>
 * 1) Be a child of {@link VisorTask} <br>
 * 2) Contain constructor with only parameter:
 * {@link VisorAddon}
 * <br><br>
 * To make it detectable by Visor, you need to implement
 * {@link VisorAddon#getAddonPackagePath()}
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterVisorTask {

}
