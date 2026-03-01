package org.vmstudio.visor.api.client.render.decoration.annotations;



import org.vmstudio.visor.api.client.render.decoration.effects.VRHandEffect;
import org.vmstudio.visor.api.common.addon.VisorAddon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * Annotation to register your {@link VRHandEffect} automatically on addon load.
 *
 * <p>
 *     Class have to:<br>
 *     1) Be a child of {@link VRHandEffect} <br>
 *     2) Contain constructor with a single parameter:
 *     {@link VisorAddon}
 * </p>
 *
 * <p>
 *     To make it detectable by Visor, you need to implement
 *     {@link VisorAddon#getAddonPackagePath()}
 * </p>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterVRHandEffect {
}
