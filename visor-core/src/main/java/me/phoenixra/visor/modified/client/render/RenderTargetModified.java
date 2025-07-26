package me.phoenixra.visor.modified.client.render;


public interface RenderTargetModified {



    void visor$setTextureId(int texid);

    void visor$setUseStencil(boolean useStencil);

    boolean visor$isUsingStencil();

    void visor$isLinearFilter(boolean linearFilter);

}
