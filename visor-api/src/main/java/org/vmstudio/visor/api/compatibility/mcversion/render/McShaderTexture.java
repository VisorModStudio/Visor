package org.vmstudio.visor.api.compatibility.mcversion.render;

//? if >=1.21.5 {
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
//?}
//? if >=1.21.6 {
import com.mojang.blaze3d.pipeline.RenderTarget;
//?}
//? if >=1.21.11 {
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
//?} elif >=1.21.6 {
/*import com.mojang.blaze3d.textures.GpuTextureView;
*///?} elif >=1.21.5 {
/*import com.mojang.blaze3d.textures.GpuTexture;
*///?}

//? if >=1.21.11 {
record McShaderTexture(GpuTextureView view, GpuSampler sampler) {

    private static final McShaderTexture[] UNITS = new McShaderTexture[12];

    static McShaderTexture of(Identifier location) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);
        return new McShaderTexture(texture.getTextureView(), texture.getSampler());
    }

    static McShaderTexture color(RenderTarget target) {
        GpuTextureView view = target.getColorTextureView();
        return view == null ? null : new McShaderTexture(view, McRenderTarget.sampler(target));
    }

    static McShaderTexture depth(RenderTarget target) {
        GpuTextureView view = target.getDepthTextureView();
        return view == null ? null : new McShaderTexture(view,
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
    }

    static McShaderTexture unit(int unit) {
        return unit >= 0 && unit < UNITS.length ? UNITS[unit] : null;
    }

    static McShaderTexture setUnit(int unit, McShaderTexture texture) {
        McShaderTexture previous = UNITS[unit];
        UNITS[unit] = texture;
        return previous;
    }

    void bind(RenderPass pass, String name) {
        pass.bindTexture(name, view, sampler);
    }
}
//?} elif >=1.21.6 {
/*record McShaderTexture(GpuTextureView view) {
    static McShaderTexture color(RenderTarget target) {
        return of(target.getColorTextureView());
    }

    static McShaderTexture depth(RenderTarget target) {
        return of(target.getDepthTextureView());
    }

    private static McShaderTexture of(GpuTextureView view) {
        return view == null ? null : new McShaderTexture(view);
    }

    static McShaderTexture unit(int unit) {
        GpuTextureView view = RenderSystem.getShaderTexture(unit);
        return view == null ? null : new McShaderTexture(view);
    }

    void bind(RenderPass pass, String name) {
        pass.bindSampler(name, view);
    }
}
*///?} elif >=1.21.5 {
/*record McShaderTexture(GpuTexture texture) {
    static McShaderTexture unit(int unit) {
        GpuTexture texture = RenderSystem.getShaderTexture(unit);
        return texture == null ? null : new McShaderTexture(texture);
    }

    void bind(RenderPass pass, String name) {
        pass.bindSampler(name, texture);
    }
}
*///?} else {
/*final class McShaderTexture {
}
*///?}
