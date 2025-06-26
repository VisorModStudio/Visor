package me.phoenixra.visor.api.client.gui.overlay.template;

import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptions;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.Collection;

/**
 * The basic interface for overlay templates.<br>
 *
 * <p>An {@code OverlayTemplate} is <em>not</em> an overlay itself;<br>
 * it is the blueprint from which one or more overlays<br>
 * (held by {@link ConfigOverlaysAccessor}) are instantiated.</p>
 *
 * <p>
 * Templates define:
 * <ul>
 *
 *  <li>The immutable base behaviour / rendering logic</li>
 *
 *  <li>A set of {@link OverlayOptions template options} that
 *      can be modified in config or in overlays settings</li>
 *
 * </ul>
 * </p>

 */
public interface OverlayTemplate extends VROverlay {


    /**
     * Get option that is an instance of <code>type</code>
     *
     * <p>
     *     You can create your own option class,
     *     or use already existing one from {@link me.phoenixra.visor.api.client.gui.overlay.template.options.types this package}
     * </p>
     *
     * @param type the option class
     *
     * @return option instance or null
     */
    @Nullable
    <T extends OverlayOptions> T getTemplateOption(@NotNull Class<T> type);

    /**
     * Get available options of this template
     *
     * @return the template options
     */
    @NotNull
    Collection<OverlayOptions> getTemplateOptions();

    /**
     * Get config of the overlay instance
     * created from this template
     *
     * @return the config file
     */
    @NotNull
    ConfigFile getConfig();


    /**
     * Get overlay name
     *
     * @return the overlay name
     */
    @NotNull
    Component getOverlayName();

    /**
     * Get template id
     *
     * @return the template id
     */
    @NotNull
    String getTemplateId();



    @ApiStatus.Internal
    @Nullable PoseAnchor getDemoAnchor();

    @ApiStatus.Internal
    void setDemoAnchor(@Nullable PoseAnchor anchor);
}
