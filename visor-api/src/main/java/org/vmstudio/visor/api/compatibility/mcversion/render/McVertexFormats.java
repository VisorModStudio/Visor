package org.vmstudio.visor.api.compatibility.mcversion.render;

//? if >=26.2 {
import com.mojang.renderpearl.api.GpuFormat;
//?} elif >=26.1 {
/*import com.mojang.blaze3d.vertex.VertexFormatElement;
*///?} else {
/*import com.mojang.blaze3d.vertex.DefaultVertexFormat;
*///?}
import com.mojang.renderpearl.api.vertex.VertexFormat;

public final class McVertexFormats {

    //? if >=26.2 {
    public static final VertexFormat POSITION_COLOR_TEX_LIGHTMAP = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT)
            .addAttribute("UV0", GpuFormat.RG32_FLOAT)
            .addAttribute("Color", GpuFormat.RGBA8_UNORM)
            .addAttribute("UV2", GpuFormat.RG16_SINT)
            .build();
    //?} elif >=26.1 {
    /*public static final VertexFormat POSITION_COLOR_TEX_LIGHTMAP = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("UV0", VertexFormatElement.UV0)
            .add("Color", VertexFormatElement.COLOR)
            .add("UV2", VertexFormatElement.UV2)
            .build();
    *///?} else {
    /*public static final VertexFormat POSITION_COLOR_TEX_LIGHTMAP = DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP;
    *///?}

    private McVertexFormats() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }
}
