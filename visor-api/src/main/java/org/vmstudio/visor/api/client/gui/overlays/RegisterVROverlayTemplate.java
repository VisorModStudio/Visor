package org.vmstudio.visor.api.client.gui.overlays;


import org.vmstudio.visor.api.common.addon.VisorAddon;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to register your {@link VROverlayTemplate} automatically on addon load.
 *
 * <p>
 *     Class have to:<br>
 *     1) Be a child of {@link VROverlayTemplate} <br>
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
public @interface RegisterVROverlayTemplate {
    /**
     * Get Template id
     *
     * @return the id
     */
    @NotNull
    String id();


    /**
     * Get template name.
     * <p>
     *     This value is used as parameter
     *     for {@link Component#translatable(String)}
     * </p>
     * @return the name
     */
    @NotNull
    String name();

    /**
     * Get template description.
     * <p>
     *     This value is used as parameter
     *     for {@link Component#translatable(String)}
     * </p>
     * @return the description.
     */
    @NotNull
    String description();

    /**
     * If overlay with default settings should be created
     * when no overlays folder found
     *
     * @return true/false
     */
    boolean isCreateDefault() default false;
}
