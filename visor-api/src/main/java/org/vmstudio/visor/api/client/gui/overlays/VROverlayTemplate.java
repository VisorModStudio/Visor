package org.vmstudio.visor.api.client.gui.overlays;


import org.vmstudio.visor.api.client.gui.OverlayConfigAccessor;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsIdentity;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * The basic interface for overlay templates.<br>
 *
 * <p>An {@code OverlayTemplate} is <em>not</em> an overlay itself;<br>
 * it is the blueprint from which one or more overlays<br>
 * (held by {@link OverlayConfigAccessor}) are instantiated.</p>
 *
 * <p>
 * Templates define:
 * <ul>
 *
 *  <li>The immutable base behaviour / rendering logic</li>
 *
 *  <li>A set of NON-EMPTY {@link OverlayOptionGroup template options} that
 *      can be modified in config file or in overlays settings</li>
 *
 * </ul>
 * </p>

 */
public interface VROverlayTemplate extends VROverlay {

    /**
     * Updates overlay identity
     * from {@link OverlayOptionsIdentity options data}
     */
    void updateIdentity();

    /**
     * Get template id
     *
     * @return the template id
     */
    @NotNull
    String getTemplateId();

    /**
     * Get template name
     *
     * @return the template name
     */
    @NotNull
    Component getTemplateName();

    /**
     * Get template description
     *
     * @return the template description
     */
    @NotNull
    Component getTemplateDescription();

}
