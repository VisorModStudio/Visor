package me.phoenixra.visor.core.client.mcmodified.render;


import org.lwjgl.opengl.GL30;

public interface RenderTargetModified {


    default void visor$genMipMaps() {
        GL30.glGenerateMipmap(3553);
    }

    void visor$setTextid(int texid);

    void visor$setUseStencil(boolean useStencil);

    boolean visor$isUseStencil();

    void visor$isLinearFilter(boolean linearFilter);

}
