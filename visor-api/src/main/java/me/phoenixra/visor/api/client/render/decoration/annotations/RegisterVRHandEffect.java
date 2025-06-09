package me.phoenixra.visor.api.client.render.decoration.annotations;



import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * Annotation to register your VR hand effects
 * <br><br>
 * Class have to:<br>
 * 1) Be a child of VRHandEffect <br>
 * 2) Contain constructor with parameter:
 * {VRAddon}
 * <br><br>
 * To make it detectable by Visor, you need to implement
 * {@link me.phoenixra.visor.api.common.addon.VisorAddon#getAddonPackagePath()}
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterVRHandEffect {
}
