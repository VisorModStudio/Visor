package org.vmstudio.visor.core.client.render.target;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;
import org.vmstudio.visor.compatibility.ShaderCompatHelper;
import org.vmstudio.visor.extensions.client.render.RenderTargetExtension;

import java.util.function.Supplier;

public class VRRenderTarget extends RenderTarget {

    private final String name;


    @Getter
    private final Supplier<Integer> textureSupplier;



    public VRRenderTarget(String name, int width, int height,
                          boolean usedepth,
                          Supplier<Integer> textureSupplier,
                          boolean linearFilter,
                          boolean useStencil) {
        //? if >=1.21.5 {
        super(name, usedepth);
        //?} else {
        /*super(usedepth);
        *///?}
        McGlState.assertOnRenderThreadOrInit();

        this.textureSupplier = textureSupplier;
        this.name = name;

        //? if >=1.21.5 {
        McRenderTarget.resize(this, width, height);
        McRenderTarget.setFilterMode(this, linearFilter);
        if (useStencil) {
            ModLoader.get().enableRenderTargetStencil(this);
        }
        //?} else {
        /*((RenderTargetExtension) this).visor$setTextureId(textureSupplier.get());
        ((RenderTargetExtension) this).visor$setLinearFilter(linearFilter);
        McRenderTarget.resize(this, width, height);
        if (useStencil) {
            if(!ModLoader.get().enableRenderTargetStencil(this)){
                ((RenderTargetExtension) this).visor$setUseStencil(true);
            }
        }
        *///?}
        McRenderTarget.setClearColor(this, 0, 0, 0, 0);

        ShaderCompatHelper.bridge().onRenderTargetCreated(this);
    }


    //? if >=1.21.5 {
    @Override
    public void createBuffers(int width, int height) {
        super.createBuffers(width, height);
        Integer adopted = textureSupplier == null ? null : textureSupplier.get();
        if (adopted != null && adopted > 0) {
            this.colorTexture.close();
            this.colorTexture = McRenderTarget.adoptForeignTexture(name, width, height, adopted);
        }
    }
    //?}

    @Override
    public String toString() {
        // Use “<unnamed>” if name is null or blank
        String displayName = (name != null && !name.isBlank()) ? name : "<unnamed>";

        return String.format(
                "Name:   %s%n" +
                        "Size:   %d x %d%n" +
                        "FB ID:  %d%n" +
                        "Tex ID: %d",
                displayName,
                viewWidth, viewHeight,
                //? if >=1.21.5 {
                0,
                //?} else {
                /*frameBufferId,
                *///?}
                McRenderTarget.colorTextureId(this)
        );
    }


}
