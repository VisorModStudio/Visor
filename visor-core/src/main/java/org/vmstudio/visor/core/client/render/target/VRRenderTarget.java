package org.vmstudio.visor.core.client.render.target;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.compatibility.ShadersHelper;
import org.vmstudio.visor.extensions.client.render.RenderTargetExtension;
import net.minecraft.client.Minecraft;

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
        super(usedepth);
        RenderSystem.assertOnRenderThreadOrInit();

        this.textureSupplier = textureSupplier;
        this.name = name;

        ((RenderTargetExtension) this).visor$setTextureId(textureSupplier.get());
        ((RenderTargetExtension) this).visor$isLinearFilter(linearFilter);
        this.resize(width, height);
        if (useStencil) {
            if(!ModLoader.get().enableRenderTargetStencil(this)){
                ((RenderTargetExtension) this).visor$setUseStencil(true);
            }
        }
        this.setClearColor(0, 0, 0, 0);

        ShadersHelper.bridge().onRenderTargetCreated(this);
    }


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
                frameBufferId,
                colorTextureId
        );
    }


}
