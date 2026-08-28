package org.vmstudio.visor.compatibility.shaders;

import org.vmstudio.visor.api.common.utils.LoggerUtils;

import java.util.List;
import java.util.Locale;


public final class ShaderPatcher {
    // rewrites the projMAD()-style macro definition itself, so every use of it expands correctly
    private static final ShaderPatch PROJMAD_MACRO = new ShaderPatch()
            .hint("#define")
            .rewrite("#define ${macro}(${mat}, ${vec}) (((${mat}) * vec4((${vec}), 1.0)).xyz)",
                    p -> p.glsl("#define").capture("macro").glsl("(").capture("mat").glsl(",")
                            .capture("vec").glsl(")(")
                            .either("diag3", "diagonal3").raw("\\w*")
                            .glsl("(").same("mat").glsl(")*(").same("vec")
                            .glsl(")+(").same("mat").glsl(")[3].xyz)"));

    // ---- view -> clip ----

    // diagonal3-times-vector plus the [3] column, with or without parens around the product
    private static final ShaderPatch FORWARD_DIAG3 = new ShaderPatch()
            .hint("[3]")
            .rewrite("(${mat} * vec4(${vec}, 1.0)).xyz",
                    p -> p.glsl("(").either("diag3", "diagonal3", "vec3").glsl("(").capture("mat")
                            .optional(o -> o.glsl("[0].x,").same("mat").glsl("[1].y,")
                                    .same("mat").glsl("[2].z"))
                            .glsl(")*")
                            .capturing("vec", c -> c.anyOf(b -> b.anyName(),
                                    b -> b.glsl("(").anyName().glsl(")")))
                            .glsl(")+")
                            .anyOf(b -> b.same("mat"),
                                    b -> b.glsl("(").same("mat").glsl(")"))
                            .glsl("[3].xyz"))
            .rewrite("(${mat} * vec4(${vec}, 1.0)).xyz",
                    p -> p.either("diag3", "diagonal3", "vec3").glsl("(").capture("mat")
                            .optional(o -> o.glsl("[0].x,").same("mat").glsl("[1].y,")
                                    .same("mat").glsl("[2].z"))
                            .glsl(")*")
                            .capturing("vec", c -> c.anyOf(b -> b.anyName(),
                                    b -> b.glsl("(").anyName().glsl(")")))
                            .glsl("+")
                            .anyOf(b -> b.same("mat"),
                                    b -> b.glsl("(").same("mat").glsl(")"))
                            .glsl("[3].xyz"));

    // homogeneous xyzz-times-diagonal4: premultiplied variable, call on either side, or spelled out
    private static final ShaderPatch FORWARD_DIAG4 = new ShaderPatch()
            .hint("[3]")
            .rewrite("${mat} * vec4(${vec}, 1.0);",
                    p -> p.optional(o -> o
                                    .anyOf(b -> b.anyName(),
                                            b -> b.either("diag4", "diagonal4")
                                                    .glsl("(").anyName().glsl(")"),
                                            b -> b.glsl("vec4(").capture("diagMat").glsl("[0].x,")
                                                    .same("diagMat").glsl("[1].y,")
                                                    .same("diagMat").glsl("[2].zw)"))
                                    .glsl("*").space())
                            .tight().capture("vec").glsl(".xyzz")
                            .optional(o -> o.glsl("*").either("diag4", "diagonal4")
                                    .glsl("(").anyName().glsl(")"))
                            .glsl("+").capture("mat").glsl("[3];"));

    // xyzz-times-diagonal4 with the [3].z column term written out
    private static final ShaderPatch FORWARD_DIAG4_Z = new ShaderPatch()
            .hint("diag")
            .rewrite("${pos} = ${mat} * vec4(${pos}.xyz, 1.0);",
                    p -> p.capture("pos").glsl("=").same("pos").glsl(".xyzz*")
                            .either("diag4", "diagonal4").glsl("(").capture("mat")
                            .glsl(")+vec4(0.0,0.0,").same("mat").glsl("[3].z,0.0);"));

    // three-statement clip assembly from a mat3 scale
    private static final ShaderPatch FORWARD_MAT3_SCALE = new ShaderPatch()
            .hint("mat3")
            .rewrite("${out} = ${mat} * vec4(${view}, 1.0);",
                    p -> p.capture("out").glsl(".xyz=").anyName().glsl("(mat3(").capture("mat")
                            .glsl("))*").capture("view").glsl(";")
                            .same("out").glsl(".z+=").same("mat").glsl("[3].z;")
                            .same("out").glsl(".w=-").same("view").glsl(".z;"));

    // projMAD-style call wrapped into vec4 with a manual [2].w w-term (Ebin)
    private static final ShaderPatch FORWARD_PROJMAD = new ShaderPatch()
            .hint("[2]")
            .rewrite("${mat} * vec4(${view}, 1.0)",
                    p -> p.glsl("vec4(").anyName().glsl("(").capture("mat").glsl(",")
                            .capture("view").glsl("),").same("view").glsl(".z*")
                            .same("mat").glsl("[2].w)"));

    // ---- screen -> view ----

    // unproject via inverse diagonal plus [3] column, divided in a second statement (rre36 packs);
    // exact for off-center frusta but not canted ones, see ProjectionMathTest
    private static final ShaderPatch INVERSE_DIAG_DIVIDE = new ShaderPatch()
            .hint("[0]")
            .rewrite("""
                    vec4 ${out}Visor = ${mat} * vec4(${screen}.xyz, 1.0);
                    vec3 ${out} = ${out}Visor.xyz / ${out}Visor.w;""",
                    p -> p.glsl("vec3").capture("out").glsl("=vec3(vec2(").capture("mat")
                            .glsl("[0].x,").same("mat").glsl("[1].y)*").capture("screen")
                            .glsl(".xy+").same("mat").glsl("[3].xy,").same("mat").glsl("[3].z);")
                            .same("out").glsl("/=").same("mat").glsl("[2].w*")
                            .same("screen").glsl(".z+").same("mat").glsl("[3].w;"));

    // inline unproject of ndc, in distributed and factored spellings
    private static final ShaderPatch INVERSE_NDC_DIAG = new ShaderPatch()
            .hint("[0]")
            .rewrite("""
                    vec4 ${out} = ${mat} * vec4(${screen} * 2.0 - 1.0, 1.0);
                    return ${out}.xyz / ${out}.w;""",
                    p -> p.glsl("vec3").capture("out").glsl("=vec3(vec2(").capture("mat")
                            .glsl("[0].x,").same("mat").glsl("[1].y)*").capture("screen")
                            .glsl(".xy*2.0-vec2(").same("mat").glsl("[0].x,").same("mat")
                            .glsl("[1].y),-1.0);return").same("out").glsl("/(").same("mat")
                            .glsl("[2].w*").same("screen").glsl(".z*2.0-").same("mat")
                            .glsl("[2].w+").same("mat").glsl("[3].w);"))
            .rewrite("""
                    vec4 ${out} = ${mat} * vec4(${screen} * 2.0 - 1.0, 1.0);
                    return ${out}.xyz / ${out}.w;""",
                    p -> p.glsl("vec3").capture("out").glsl("=vec3(vec2(").capture("mat")
                            .glsl("[0].x,").same("mat").glsl("[1].y)*(").capture("screen")
                            .glsl(".xy*2.0-1.0),-1.0);return").same("out").glsl("/(")
                            .same("mat").glsl("[2].w*(").same("screen").glsl(".z*2.0-1.0)+")
                            .same("mat").glsl("[3].w);"));

    // projMAD-style unproject followed by the perspective divide (Ebin)
    private static final ShaderPatch INVERSE_PROJMAD = new ShaderPatch()
            .hint("[2]")
            .rewrite("""
                    vec4 ${screen}Visor = ${mat} * vec4(${screen}, 1.0);
                    return ${screen}Visor.xyz / ${screen}Visor.w;""",
                    p -> p.glsl("return").anyName().glsl("(").capture("mat").glsl(",")
                            .capture("screen").glsl(")/(").same("screen").glsl(".z*")
                            .same("mat").glsl("[2].w+").same("mat").glsl("[3].w);"));

    // unproject from separate texcoord and depth
    private static final ShaderPatch INVERSE_TEXCOORD_DIAG = new ShaderPatch()
            .hint("[0]")
            .rewrite("""
                    vec4 ${out}Visor = ${mat} * vec4(vec3(${uv}, ${depth}) * 2.0 - 1.0, 1.0);
                    ${out} = ${out}Visor.xyz / ${out}Visor.w;""",
                    p -> p.capture("out").glsl("=vec3(vec2(").capture("mat").glsl("[0].x,")
                            .same("mat").glsl("[1].y)*(").capture("uv").glsl("*2.0-1.0)+")
                            .same("mat").glsl("[3].xy,").same("mat").glsl("[3].z);")
                            .same("out").glsl("/=(").same("mat").glsl("[2].w*(")
                            .capture("depth").glsl("*2.0-1.0)+").same("mat").glsl("[3].w);"));

    // ---- view -> screen uv ----

    // two-statement uv from the diagonal, xy only
    private static final ShaderPatch SCREEN_XY_DIAG = new ShaderPatch()
            .hint("[0]")
            .rewrite("""
                    vec4 ${clip} = ${mat} * vec4(${view}, 1.0);
                    return (${clip}.xy / ${clip}.w) * 0.5 + 0.5;""",
                    p -> p.glsl("vec2").capture("clip").glsl("=vec2(").capture("mat")
                            .glsl("[0].x,").same("mat").glsl("[1].y)*").capture("view")
                            .glsl(".xy;return 0.5-(").same("clip").glsl(".xy/")
                            .same("view").glsl(".z)")
                            .anyOf(b -> b.glsl("/2.0"), b -> b.glsl("*0.5")).glsl(";"));

    // paired line-endpoint clips; the 0.99609375 factor keeps the pack's clip/depth ratio,
    // whose divisor is scaled by the same constant
    private static final ShaderPatch SCREEN_LINE_XY_DIAG = new ShaderPatch()
            .hint("0.99609375")
            .rewrite("""
                    vec2 ${clipA} = (${mat} * vec4(${viewA}, 1.0)).xy * 0.99609375;
                    vec2 ${clipB} = (${mat} * vec4(${viewB}, 1.0)).xy * 0.99609375;${tail}""",
                    p -> p.glsl("vec2").capture("clipA").glsl("=vec2(").capture("mat")
                            .glsl("[0].x,").same("mat").glsl("[1].y)*").capture("viewA")
                            .glsl(".xy;")
                            .glsl("vec2").capture("clipB").glsl("=vec2(").same("mat")
                            .glsl("[0].x,").same("mat").glsl("[1].y)*").capture("viewB")
                            .glsl(".xy;")
                            .raw("(?<tail>").anything().same("viewA")
                            .glsl(".z*0.99609375;").raw(")"));

    // two-statement uv from the diagonal including the depth term
    private static final ShaderPatch SCREEN_XYZ_DIAG = new ShaderPatch()
            .hint("[0]")
            .rewrite("""
                    vec4 ${clip} = ${mat} * vec4(${view}, 1.0);
                    return (${clip}.xyz / ${clip}.w) * 0.5 + 0.5;""",
                    p -> p.glsl("vec2").capture("clip").glsl("=vec2(").capture("mat")
                            .glsl("[0].x,").same("mat").glsl("[1].y)*").capture("view")
                            .glsl(".xy;return 0.5-vec3(").same("clip").glsl(".xy/")
                            .same("view").glsl(".z,").same("mat").glsl("[3].z/")
                            .same("view").glsl(".z+").same("mat").glsl("[2].z)")
                            .anyOf(b -> b.glsl("/2.0"), b -> b.glsl("*0.5")).glsl(";"));

    // single-return uv from a diagonal2 call
    private static final ShaderPatch SCREEN_XY_DIAG2 = new ShaderPatch()
            .hint("diag")
            .rewrite("""
                    vec4 ${view}Visor = ${mat} * vec4(${view}, 1.0);
                    return (${view}Visor.xy / ${view}Visor.w) * 0.5 + 0.5;""",
                    p -> p.glsl("return(").either("diag2", "diagonal2").glsl("(").capture("mat")
                            .glsl(")*").capture("view").glsl(".xy+").same("mat")
                            .glsl("[3].xy)/-").same("view").glsl(".z*0.5+0.5;"));

    // previous-frame reprojection writing clip and uv separately
    private static final ShaderPatch SCREEN_PREVIOUS_FRAME = new ShaderPatch()
            .hint("[0]")
            .rewrite("""
                    vec4 ${view}Visor = ${mat} * vec4(${view}, 1.0);
                    ${clip} = ${view}Visor.xy;
                    ${uv} = ${view}Visor.xy / ${view}Visor.w * 0.5 + 0.5;""",
                    p -> p.capture("clip").glsl("=vec2(").capture("mat").glsl("[0].x,")
                            .same("mat").glsl("[1].y)*").capture("view").glsl(".xy+")
                            .same("mat").glsl("[3].xy;").capture("uv").glsl("=(")
                            .same("clip").glsl("/-").same("view").glsl(".z)*0.5+0.5;"));

    private static final List<ShaderPatch> PATCHES = List.of(
            // macro definitions first, then grouped by direction
            PROJMAD_MACRO,
            // view -> clip
            FORWARD_DIAG3,
            FORWARD_DIAG4,
            FORWARD_DIAG4_Z,
            FORWARD_MAT3_SCALE,
            FORWARD_PROJMAD,
            // screen -> view
            INVERSE_DIAG_DIVIDE,
            INVERSE_NDC_DIAG,
            INVERSE_PROJMAD,
            INVERSE_TEXCOORD_DIAG,
            // view -> screen uv
            SCREEN_XY_DIAG,
            SCREEN_XY_DIAG2,
            SCREEN_XYZ_DIAG,
            SCREEN_LINE_XY_DIAG,
            SCREEN_PREVIOUS_FRAME);

    private ShaderPatcher() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String patchShader(String shader) {
        if (shader == null) return null;

        String patched = shader;
        String lower = shader.toLowerCase(Locale.ROOT);
        for (ShaderPatch patch : PATCHES) {
            try {
                String result = patch.applyTo(patched, lower);
                // identity check: replaceAll hands back the same instance when nothing matched
                if (result != patched) {
                    patched = result;
                    lower = patched.toLowerCase(Locale.ROOT);
                }
            } catch (Throwable t) {
                LoggerUtils.getLogger().error("Visor: a shader patch rule failed, skipping it", t);
            }
        }
        return patched;
    }
}
