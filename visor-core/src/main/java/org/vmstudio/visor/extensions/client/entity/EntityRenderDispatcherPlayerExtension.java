package org.vmstudio.visor.extensions.client.entity;



import org.vmstudio.visor.core.client.render.player.VRPlayerRenderer;

import java.util.Map;

public interface EntityRenderDispatcherPlayerExtension {

    /**
     * @return map of VR player renderers with the vanilla model
     */
    Map<String, VRPlayerRenderer> visor$getSkinMapVRVanilla();

    /**
     * @return map of VR player renderers with split arms
     */
    Map<String, VRPlayerRenderer> visor$getSkinMapVRArms();

    /**
     * @return map of VR player renderers with split arms and legs
     */
    Map<String, VRPlayerRenderer> visor$getSkinMapVRLegs();

}
