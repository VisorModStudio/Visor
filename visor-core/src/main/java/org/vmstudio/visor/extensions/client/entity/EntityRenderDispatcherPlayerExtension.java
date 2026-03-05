package org.vmstudio.visor.extensions.client.entity;



import org.vmstudio.visor.core.client.render.player.VRPlayerRenderer;

import java.util.Map;

public interface EntityRenderDispatcherPlayerExtension {

    Map<String, VRPlayerRenderer> visor$getSkinMapVR();

}
