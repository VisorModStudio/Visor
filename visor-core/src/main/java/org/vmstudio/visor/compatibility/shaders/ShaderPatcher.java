package org.vmstudio.visor.compatibility.shaders;

import org.vmstudio.visor.api.common.utils.LoggerUtils;

import java.util.List;

public final class ShaderPatcher {
    /* #define projMAD(m, v) (diagonal3(m) * (v) + (m)[3].xyz) */
    private static final ShaderPatch PROJMAD_MACRO = new ShaderPatch()
            .rewrite("#define ${macro}(${mat}, ${vec}) (((${mat}) * vec4((${vec}), 1.0)).xyz)",
                    p -> p.glsl("#define").capture("macro").glsl("(").capture("mat").glsl(",")
                            .capture("vec").glsl(")(")
                            .either("diagonal3", "diag3").raw("\\w*")
                            .glsl("(").same("mat").glsl(")*(").same("vec")
                            .glsl(")+(").same("mat").glsl(")[3].xyz)"));

    // ---- view -> clip ----

    /* pos = pos.xyzz * diag4(gl_ProjectionMatrix) + vec4(0.0, 0.0, gl_ProjectionMatrix[3].z, 0.0); */
    private static final ShaderPatch FORWARD_DIAG4_Z = new ShaderPatch()
            .rewrite("${pos} = ${mat} * vec4(${pos}.xyz, 1.0);",
                    p -> p.capture("pos").glsl("=").same("pos").glsl(".xyzz*")
                            .either("diag4", "diagonal4").glsl("(").capture("mat")
                            .glsl(")+vec4(0.0,0.0,").same("mat").glsl("[3].z,0.0);"));

    /* gl_Position.xyz = getMatScale(mat3(gl_ProjectionMatrix)) * vertexViewPos;
     * gl_Position.z += gl_ProjectionMatrix[3].z;
     * gl_Position.w = -vertexViewPos.z;
     */
    private static final ShaderPatch FORWARD_MAT3_SCALE = new ShaderPatch()
            .rewrite("${out} = ${mat} * vec4(${view}, 1.0);",
                    p -> p.capture("out").glsl(".xyz=").anyName().glsl("(mat3(").capture("mat")
                            .glsl("))*").capture("view").glsl(";")
                            .same("out").glsl(".z+=").same("mat").glsl("[3].z;")
                            .same("out").glsl(".w=-").same("view").glsl(".z;"));

    /* viewPos = diagonal4(projInv) * clip.xyzz + projInv[3];
     * viewPos = clip.xyzz * diagonal4(projInv) + projInv[3];
     * viewPos = vec4(projInv[0].x, projInv[1].y, projInv[2].zw) * clip.xyzz + projInv[3];
     * viewPos = iProjDiag * clip.xyzz + projInv[3];
     */
    private static final ShaderPatch FORWARD_DIAG4 = new ShaderPatch()
            .rewrite("${mat} * vec4(${vec}, 1.0);",
                    p -> p.optional(o -> o
                                    .anyOf(b -> b.either("diagonal4", "diag4")
                                                    .glsl("(").anyName().glsl(")"),
                                            b -> b.anyName(),
                                            b -> b.glsl("vec4(").capture("diagMat").glsl("[0].x,")
                                                    .same("diagMat").glsl("[1].y,")
                                                    .same("diagMat").glsl("[2].zw)"))
                                    .glsl("*").space())
                            .tight().capture("vec").glsl(".xyzz")
                            .optional(o -> o.glsl("*").either("diagonal4", "diag4")
                                    .glsl("(").anyName().glsl(")"))
                            .glsl("+").capture("mat").glsl("[3];"));

    /* clipPos = vec4(projMAD(gl_ProjectionMatrix, viewPos), viewPos.z * gl_ProjectionMatrix[2].w); */
    private static final ShaderPatch FORWARD_PROJMAD = new ShaderPatch()
            .rewrite("${mat} * vec4(${view}, 1.0)",
                    p -> p.glsl("vec4(").anyName().glsl("(").capture("mat").glsl(",")
                            .capture("view").glsl("),").same("view").glsl(".z*")
                            .same("mat").glsl("[2].w)"));

    // ---- screen -> view ----

    /* vec3 viewPos = vec3(vec2(projInv[0].x, projInv[1].y) * screenPos.xy + projInv[3].xy, projInv[3].z);
     * viewPos /= projInv[2].w * screenPos.z + projInv[3].w;
     */
    private static final ShaderPatch INVERSE_DIAG_DIVIDE = new ShaderPatch()
            .rewrite("""
                    vec4 visorUnproject${out} = ${mat} * vec4(${screen}.xyz, 1.0);
                    vec3 ${out} = visorUnproject${out}.xyz / visorUnproject${out}.w;""",
                    p -> p.glsl("vec3").capture("out").glsl("=vec3(vec2(").capture("mat")
                            .glsl("[0].x,").same("mat").glsl("[1].y)*").capture("screen")
                            .glsl(".xy+").same("mat").glsl("[3].xy,").same("mat").glsl("[3].z);")
                            .same("out").glsl("/=").same("mat").glsl("[2].w*")
                            .same("screen").glsl(".z+").same("mat").glsl("[3].w;"));

    /* vec3 viewPos = vec3(vec2(projInv[0].x, projInv[1].y) * (screenPos.xy * 2.0 - 1.0), -1.0);
     * return viewPos / (projInv[2].w * (screenPos.z * 2.0 - 1.0) + projInv[3].w);
     *
     * vec3 viewPos = vec3(vec2(projInv[0].x, projInv[1].y) * screenPos.xy * 2.0 - vec2(projInv[0].x, projInv[1].y), -1.0);
     * return viewPos / (projInv[2].w * screenPos.z * 2.0 - projInv[2].w + projInv[3].w);
     */
    private static final ShaderPatch INVERSE_NDC_DIAG = new ShaderPatch()
            .rewrite("""
                    vec4 ${out} = ${mat} * vec4(${screen} * 2.0 - 1.0, 1.0);
                    return ${out}.xyz / ${out}.w;""",
                    p -> p.glsl("vec3").capture("out").glsl("=vec3(vec2(").capture("mat")
                            .glsl("[0].x,").same("mat").glsl("[1].y)*(").capture("screen")
                            .glsl(".xy*2.0-1.0),-1.0);return").same("out").glsl("/(")
                            .same("mat").glsl("[2].w*(").same("screen").glsl(".z*2.0-1.0)+")
                            .same("mat").glsl("[3].w);"))
            .rewrite("""
                    vec4 ${out} = ${mat} * vec4(${screen} * 2.0 - 1.0, 1.0);
                    return ${out}.xyz / ${out}.w;""",
                    p -> p.glsl("vec3").capture("out").glsl("=vec3(vec2(").capture("mat")
                            .glsl("[0].x,").same("mat").glsl("[1].y)*").capture("screen")
                            .glsl(".xy*2.0-vec2(").same("mat").glsl("[0].x,").same("mat")
                            .glsl("[1].y),-1.0);return").same("out").glsl("/(").same("mat")
                            .glsl("[2].w*").same("screen").glsl(".z*2.0-").same("mat")
                            .glsl("[2].w+").same("mat").glsl("[3].w);"));

    /* return projMAD(gbufferProjectionInverse, screenPos)
     *      / (screenPos.z * gbufferProjectionInverse[2].w + gbufferProjectionInverse[3].w);
     */
    private static final ShaderPatch INVERSE_PROJMAD = new ShaderPatch()
            .rewrite("""
                    vec4 visorUnproject${screen} = ${mat} * vec4(${screen}, 1.0);
                    return visorUnproject${screen}.xyz / visorUnproject${screen}.w;""",
                    p -> p.glsl("return").anyName().glsl("(").capture("mat").glsl(",")
                            .capture("screen").glsl(")/(").same("screen").glsl(".z*")
                            .same("mat").glsl("[2].w+").same("mat").glsl("[3].w);"));

    /* viewPos = vec3(vec2(projInv[0].x, projInv[1].y) * (texcoord * 2.0 - 1.0) + projInv[3].xy, projInv[3].z);
     * viewPos /= (projInv[2].w * (depth * 2.0 - 1.0) + projInv[3].w);
     */
    private static final ShaderPatch INVERSE_TEXCOORD_DIAG = new ShaderPatch()
            .rewrite("""
                    vec4 visorUnprojected = ${mat} * vec4(vec3(${uv}, ${depth}) * 2.0 - 1.0, 1.0);
                    ${out} = visorUnprojected.xyz / visorUnprojected.w;""",
                    p -> p.capture("out").glsl("=vec3(vec2(").capture("mat").glsl("[0].x,")
                            .same("mat").glsl("[1].y)*(").capture("uv").glsl("*2.0-1.0)+")
                            .same("mat").glsl("[3].xy,").same("mat").glsl("[3].z);")
                            .same("out").glsl("/=(").same("mat").glsl("[2].w*(")
                            .capture("depth").glsl("*2.0-1.0)+").same("mat").glsl("[3].w);"));

    // ---- view -> screen uv ----

    /* vec2 clipCoord = vec2(proj[0].x, proj[1].y) * viewPos.xy;
     * return 0.5 - (clipCoord.xy / viewPos.z) * 0.5;
     */
    private static final ShaderPatch SCREEN_XY_DIAG = new ShaderPatch()
            .rewrite("""
                    vec4 ${clip} = ${mat} * vec4(${view}, 1);
                    return (${clip}.xy / ${clip}.w) * 0.5 + 0.5;""",
                    p -> p.glsl("vec2").capture("clip").glsl("=vec2(").capture("mat")
                            .glsl("[0].x,").same("mat").glsl("[1].y)*").capture("view")
                            .glsl(".xy;return 0.5-(").same("clip").glsl(".xy/")
                            .same("view").glsl(".z)")
                            .anyOf(b -> b.glsl("*0.5"), b -> b.glsl("/2.0")).glsl(";"));

    /* vec2 clipStart = vec2(proj[0].x, proj[1].y) * lineStart.xy;
     * vec2 clipEnd = vec2(proj[0].x, proj[1].y) * lineEnd.xy;
     * float viewDepth = lineStart.z * 0.99609375;
     */
    private static final ShaderPatch SCREEN_LINE_XY_DIAG = new ShaderPatch()
            .rewrite("""
                    vec2 ${clipA} = (${mat} * vec4(${viewA}, 1.0)).xy * 0.99609375;
                    vec2 ${clipB} = (${mat} * vec4(${viewB}, 1.0)).xy * 0.99609375;
                    ${tail}""",
                    p -> p.glsl("vec2").capture("clipA").glsl("=vec2(").capture("mat")
                            .glsl("[0].x,").same("mat").glsl("[1].y)*").capture("viewA")
                            .glsl(".xy;")
                            .glsl("vec2").capture("clipB").glsl("=vec2(").same("mat")
                            .glsl("[0].x,").same("mat").glsl("[1].y)*").capture("viewB")
                            .glsl(".xy;")
                            .raw("(?<tail>").anything().same("viewA")
                            .glsl(".z*0.99609375;").raw(")"));

    /* vec2 clipCoord = vec2(proj[0].x, proj[1].y) * viewPos.xy;
     * return 0.5 - vec3(clipCoord.xy / viewPos.z, proj[3].z / viewPos.z + proj[2].z) * 0.5;
     */
    private static final ShaderPatch SCREEN_XYZ_DIAG = new ShaderPatch()
            .rewrite("""
                    vec4 ${clip} = ${mat} * vec4(${view}, 1);
                    return (${clip}.xyz / ${clip}.w) * 0.5 + 0.5;""",
                    p -> p.glsl("vec2").capture("clip").glsl("=vec2(").capture("mat")
                            .glsl("[0].x,").same("mat").glsl("[1].y)*").capture("view")
                            .glsl(".xy;return 0.5-vec3(").same("clip").glsl(".xy/")
                            .same("view").glsl(".z,").same("mat").glsl("[3].z/")
                            .same("view").glsl(".z+").same("mat").glsl("[2].z)")
                            .anyOf(b -> b.glsl("*0.5"), b -> b.glsl("/2.0")).glsl(";"));

    /* return (diagonal2(gbufferProjection) * viewPos.xy + gbufferProjection[3].xy)
     *      / -viewPos.z * 0.5 + 0.5;
     */
    private static final ShaderPatch SCREEN_XY_DIAG2 = new ShaderPatch()
            .rewrite("""
                    vec4 visorClipPos = ${mat} * vec4(${view}, 1.0);
                    return (visorClipPos.xy / visorClipPos.w) * 0.5 + 0.5;""",
                    p -> p.glsl("return(").either("diagonal2", "diag2").glsl("(").capture("mat")
                            .glsl(")*").capture("view").glsl(".xy+").same("mat")
                            .glsl("[3].xy)/-").same("view").glsl(".z*0.5+0.5;"));

    /* diagonal3(m) * (v) + (m)[3].xyz
     * diagonal3(mat) * v + mat[3].xyz
     * (diagonal3(m) * v) + m[3].xyz
     * vec3(proj[0].x, proj[1].y, proj[2].z) * viewPos + proj[3].xyz
     */
    private static final ShaderPatch FORWARD_DIAG3 = new ShaderPatch()
            .rewrite("(${mat} * vec4(${plain}${wrapped}, 1.0)).xyz",
                    p -> p.either("diagonal3", "diag3", "vec3").glsl("(").capture("mat")
                            .optional(o -> o.glsl("[0].x,").same("mat").glsl("[1].y,")
                                    .same("mat").glsl("[2].z"))
                            .glsl(")*")
                            .anyOf(b -> b.capture("plain"),
                                    b -> b.glsl("(").capture("wrapped").glsl(")"))
                            .glsl("+")
                            .anyOf(b -> b.glsl("(").same("mat").glsl(")"),
                                    b -> b.same("mat"))
                            .glsl("[3].xyz"))
            .rewrite("(${mat} * vec4(${plain}${wrapped}, 1.0)).xyz",
                    p -> p.glsl("(").either("diagonal3", "diag3", "vec3").glsl("(").capture("mat")
                            .optional(o -> o.glsl("[0].x,").same("mat").glsl("[1].y,")
                                    .same("mat").glsl("[2].z"))
                            .glsl(")*")
                            .anyOf(b -> b.capture("plain"),
                                    b -> b.glsl("(").capture("wrapped").glsl(")"))
                            .glsl(")+")
                            .anyOf(b -> b.glsl("(").same("mat").glsl(")"),
                                    b -> b.same("mat"))
                            .glsl("[3].xyz"));

    /* clipPos = vec2(prevProj[0].x, prevProj[1].y) * prevViewPos.xy + prevProj[3].xy;
     * prevTexcoord = (clipPos / -prevViewPos.z) * 0.5 + 0.5;
     */
    private static final ShaderPatch SCREEN_PREVIOUS_FRAME = new ShaderPatch()
            .rewrite("""
                    vec4 visorPrevClip = ${mat} * vec4(${view}, 1.0);
                    ${clip} = visorPrevClip.xy;
                    ${uv} = visorPrevClip.xy / visorPrevClip.w * 0.5 + 0.5;""",
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
        for (ShaderPatch patch : PATCHES) {
            try {
                patched = patch.applyTo(patched);
            } catch (Throwable t) {
                LoggerUtils.getLogger().error("Visor: a shader patch rule failed, skipping it", t);
            }
        }
        return patched;
    }
}
