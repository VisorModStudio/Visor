package org.vmstudio.visor.extensions.client.render;


import org.vmstudio.visor.api.client.input.HandAction;

public interface ItemInHandRendererExtension {
    void visor$setSwingType(HandAction interact);
}
