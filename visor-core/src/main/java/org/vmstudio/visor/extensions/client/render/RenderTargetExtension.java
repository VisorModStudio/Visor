package org.vmstudio.visor.extensions.client.render;


public interface RenderTargetExtension {



    void visor$setTextureId(int texid);

    void visor$setUseStencil(boolean useStencil);

    boolean visor$isUsingStencil();

    void visor$setLinearFilter(boolean linearFilter);

}
