package me.phoenixra.visor.api.client.gui.overlay.template;


import me.phoenixra.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to register your {@link OverlayTemplate} automatically on addon load.
 *
 * <p>
 *     Class have to:<br>
 *     1) Be a child of {@link OverlayTemplate} <br>
 *     2) Contain constructor with 2 parameters:
 *     {@link VisorAddon},
 *     {@link String}
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
public @interface RegisterOverlayTemplate {
    @NotNull
    String id();
}
