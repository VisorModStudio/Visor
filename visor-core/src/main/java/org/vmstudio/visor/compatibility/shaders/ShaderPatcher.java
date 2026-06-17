package org.vmstudio.visor.compatibility.shaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rewrites known VR-incompatible GLSL in shaderpack sources so a wider range of packs renders correctly
 * in VR. Driven from {@code JcppProcessorVRMixin}, which feeds it every shader source Iris preprocesses
 * while VR is active
*/
public final class ShaderPatcher {
    /**
     * Rewrites the {@code projMAD(m, v)} macro definition to use a full matrix multiply instead of a centered-frustum
     * Fixes all call sites at once across multiple packs (BSL, Complementary, etc.) by preserving captured macro/parameter names
     */
    private static final ShaderPatch REDEFINE_PROJMAD_MACRO = ShaderPatch.of("macro define: projMAD-style shortcut -> full matrix multiply", """
            #define projMAD(m, v) (diagonal3(m) * (v) + (m)[3].xyz)""", """
            #define $1($2, $3) ((($2) * vec4(($3), 1.0)).xyz)""",
        "#define (\\w+)\\((\\w+),(\\w+)\\)\\((?:diagonal3|diag3)\\w*\\(\\2\\)\\*\\(\\3\\)\\+\\(\\2\\)\\[3]\\.xyz\\)");

    private static final ShaderPatch PROJECT_VIA_DIAGONAL4_Z = ShaderPatch.of("forward: diagonal4 scale + z translation", """
            pos     = pos.xyzz * diag4(gl_ProjectionMatrix) + vec4(0.0, 0.0, gl_ProjectionMatrix[3].z, 0.0);
            """, """
            $1 = $3 * vec4($1.xyz, 1.0);""",
        "(\\w+)=\\1\\.xyzz\\*(diag4|diagonal4)\\((\\w+)\\)\\+vec4\\(0\\.0,0\\.0,\\3\\[3]\\.z,0\\.0\\);");

    private static final ShaderPatch PROJECT_VIA_MAT3_SCALE = ShaderPatch.of("forward: mat3 scale + z + w reconstruction", """
            gl_Position.xyz = getMatScale(mat3(gl_ProjectionMatrix)) * vertexViewPos;
            gl_Position.z += gl_ProjectionMatrix[3].z;
            gl_Position.w = -vertexViewPos.z;
            """, """
            $1 = $3 * vec4($4, 1.0);""",
        "(\\w+)\\.xyz=(\\w+)\\(mat3\\((\\w+)\\)\\)\\*(\\w+);\\1\\.z\\+=\\3\\[3]\\.z;\\1\\.w=-\\4\\.z;");

    private static final ShaderPatch PROJECT_VIA_DIAGONAL4 = ShaderPatch.of("forward: diagonal4 scale + translation", """
            diagonal4(mat) * v.xyzz + mat[3];
            v.xyzz * diagonal4(mat) + mat[3];
            vec4(m[0].x, m[1].y, m[2].zw) * pos.xyzz + m[3];
            iProjDiag * p3.xyzz + gbufferProjectionInverse[3];
            """, """
            $10 * vec4($6, 1.0);""",
        "((((diagonal4|diag4)\\(\\w+\\))|\\w+|vec4\\((\\w+)\\[0]\\.x,\\5\\[1]\\.y,\\5\\[2]\\.zw\\))\\*\\s*)?(\\w+)\\.xyzz(\\*((diagonal4|diag4)\\(\\w+\\)))?\\+(\\w+)\\[3];");

    private static final ShaderPatch PROJECT_VIA_PROJMAD = ShaderPatch.of("forward: projMAD + manual w", """
            vec4(projMAD(gl_ProjectionMatrix, viewSpacePosition), viewSpacePosition.z * gl_ProjectionMatrix[2].w);
            """, """
            $2 * vec4($3, 1.0)""",
        "vec4\\((\\w+)\\((\\w+),(\\w+)\\),\\3\\.z\\*\\2\\[2]\\.w\\)");

    private static final ShaderPatch UNPROJECT_VIA_DIAGONAL_DIVIDE = ShaderPatch.of("inverse: diagonal + perspective divide", """
            vec3 viewpos    = vec3(vec2(projInv[0].x, projInv[1].y)*screenpos.xy + projInv[3].xy, projInv[3].z);
            viewpos    /= projInv[2].w*screenpos.z + projInv[3].w;
            """, """
            vec4 visor_$1 = $2 * vec4($3.xyz, 1.0);
            vec3 $1 = visor_$1.xyz / visor_$1.w;""",
        "vec3(\\w+)=vec3\\(vec2\\((\\w+)\\[0]\\.x,\\2\\[1]\\.y\\)\\*(\\w+)\\.xy\\+\\2\\[3]\\.xy,\\2\\[3]\\.z\\);\\1/=\\2\\[2]\\.w\\*\\3\\.z\\+\\2\\[3]\\.w;");

    private static final ShaderPatch UNPROJECT_NDC_VIA_DIAGONAL = ShaderPatch.of("inverse: NDC via diagonal", """
            vec3 viewPos = vec3(vec2(projectionInverse[0].x, projectionInverse[1].y) * (screenPos.xy * 2.0 - 1.0), -1);
            return viewPos / (projectionInverse[2].w * (screenPos.z * 2.0 - 1.0) + projectionInverse[3].w);
            vec3 viewPos = vec3(vec2(projectionInverse[0].x, projectionInverse[1].y) * screenPos.xy * 2.0 - vec2(projectionInverse[0].x, projectionInverse[1].y), -1);
            return viewPos / (projectionInverse[2].w * screenPos.z * 2.0 - projectionInverse[2].w + projectionInverse[3].w);
            """, """
            vec4 $1 = $2 * vec4($3 * 2.0 - 1.0, 1.0);
            return $1.xyz / $1.w;""",
        "vec3\\s+(\\w+)=vec3\\(vec2\\((\\w+)\\[0]\\.x,\\2\\[1]\\.y\\)\\*\\((\\w+)\\.xy\\*2\\.0-1\\.0\\),-1\\.0\\);return\\s+\\1/\\(\\2\\[2]\\.w\\*\\(\\3\\.z\\*2\\.0-1\\.0\\)\\+\\2\\[3]\\.w\\);",
        "vec3\\s+(\\w+)=vec3\\(vec2\\((\\w+)\\[0]\\.x,\\2\\[1]\\.y\\)\\*(\\w+)\\.xy\\*2\\.0-vec2\\(\\2\\[0]\\.x,\\2\\[1]\\.y\\),-1\\.0\\);return\\s+\\1/\\(\\2\\[2]\\.w\\*\\3\\.z\\*2\\.0-\\2\\[2]\\.w\\+\\2\\[3]\\.w\\);");

    private static final ShaderPatch UNPROJECT_VIA_PROJMAD = ShaderPatch.of("inverse: projMAD + manual w", """
            return projMAD(gbufferProjectionInverse, screenPos) / (screenPos.z * gbufferProjectionInverse[2].w + gbufferProjectionInverse[3].w);
            """, """
            vec4 visor_$3 = $2 * vec4($3, 1.0);
            return visor_$3.xyz / visor_$3.w;""",
        "return\\s+(\\w+)\\((\\w+),(\\w+)\\)/\\(\\3\\.z\\*\\2\\[2]\\.w\\+\\2\\[3]\\.w\\);");

    private static final ShaderPatch UNPROJECT_TEXCOORD_VIA_DIAGONAL = ShaderPatch.of("inverse: texcoord via diagonal", """
            curr_view_pos =
            vec3(vec2(gbufferProjectionInverse[0].x, gbufferProjectionInverse[1].y) * (texcoord * 2.0 - 1.0) + gbufferProjectionInverse[3].xy, gbufferProjectionInverse[3].z);
            curr_view_pos /= (gbufferProjectionInverse[2].w * (z_depth * 2.0 - 1.0) + gbufferProjectionInverse[3].w);
            """, """
            vec4 visorViewPos = $2 * vec4(vec3($3, $4) * 2.0 - 1.0, 1.0);
            $1 = visorViewPos.xyz / visorViewPos.w;""",
        "(\\w+)=vec3\\(vec2\\((\\w+)\\[0]\\.x,\\2\\[1]\\.y\\)\\*\\((\\w+)\\*2\\.0-1\\.0\\)\\+\\2\\[3]\\.xy,\\2\\[3]\\.z\\);\\1/=\\(\\2\\[2]\\.w\\*\\((\\w+)\\*2\\.0-1\\.0\\)\\+\\2\\[3]\\.w\\);");

    private static final ShaderPatch PROJECT_TO_SCREEN_XY = ShaderPatch.of("screen: xy via diagonal", """
            vec2 clipCoord = vec2(projection[0].x, projection[1].y) * viewPos.xy;
            return 0.5 - (clipCoord.xy / viewPos.z) * 0.5;
            """, """
            vec4 $1 = $2 * vec4($3, 1);
            return ($1.xy / $1.w) * 0.5 + 0.5;""",
        "vec2\\s+(\\w+)=vec2\\((\\w+)\\[0]\\.x,\\2\\[1]\\.y\\)\\*(\\w+)\\.xy;return\\s+0\\.5-\\(\\1\\.xy/\\3\\.z\\)(\\*0\\.5|/2\\.0);");

    private static final ShaderPatch PROJECT_LINE_TO_SCREEN_XY = ShaderPatch.of("screen: line xy via diagonal", """
            vec2 vertexClipCoordStart = vec2(projectionMatrix[0].x, projectionMatrix[1].y) * linePosStart.xy;
            vec2 vertexClipCoordEnd = vec2(projectionMatrix[0].x, projectionMatrix[1].y) * linePosEnd.xy;
            float vertexViewDepth = linePosStart.z * 0.99609375; // don't patch
            """, """
            vec2 $1 = ($2 * vec4($3, 1.0)).xy * 0.99609375;
            vec2 $4 = ($2 * vec4($5, 1.0)).xy * 0.99609375;
            $6""",
        "vec2\\s+(\\w+)=vec2\\((\\w+)\\[0]\\.x,\\2\\[1]\\.y\\)\\*(\\w+)\\.xy;vec2\\s+(\\w+)=vec2\\(\\2\\[0]\\.x,\\2\\[1]\\.y\\)\\*(\\w+)\\.xy;(((.|\\s)+?)\\3\\.z\\*0\\.99609375;)");

    private static final ShaderPatch PROJECT_TO_SCREEN_XYZ = ShaderPatch.of("screen: xyz via diagonal", """
            vec2 clipCoord = vec2(projection[0].x, projection[1].y) * viewPos.xy;
            return 0.5 - vec3(clipCoord.xy / viewPos.z, projection[3].z / viewPos.z + projection[2].z) * 0.5;
            """, """
            vec4 $1 = $2 * vec4($3, 1);
            return ($1.xyz / $1.w) * 0.5 + 0.5;""",
        "vec2\\s+(\\w+)=vec2\\((\\w+)\\[0]\\.x,\\2\\[1]\\.y\\)\\*(\\w+)\\.xy;return\\s+0\\.5-vec3\\(\\1\\.xy/\\3\\.z,\\2\\[3]\\.z/\\3\\.z\\+\\2\\[2]\\.z\\)(\\*0\\.5|/2\\.0);");

    private static final ShaderPatch PROJECT_TO_SCREEN_VIA_DIAGONAL2 = ShaderPatch.of("screen: xy via diagonal2 + translation", """
            return (diagonal2(gbufferProjection) * viewSpacePosition.xy + gbufferProjection[3].xy) / -viewSpacePosition.z * 0.5 + 0.5;
            """, """
            vec4 visor_pos = $2 * vec4($3, 1.0);
            return (visor_pos.xy / visor_pos.w) * 0.5 + 0.5;""",
        "return\\s+\\((diagonal2|diag2)\\((\\w+)\\)\\*(\\w+)\\.xy\\+\\2\\[3]\\.xy\\)/-\\3\\.z\\*0\\.5\\+0\\.5;");

    private static final ShaderPatch PROJECT_VIA_DIAGONAL3 = ShaderPatch.of("forward: diagonal3 scale + translation", """
            diagonal3(m) * (v) + (m)[3].xyz
            diagonal3(mat) * v + mat[3].xyz
            (diagonal3(m) * v) + m[3].xyz;
            vec3(projection[0].x, projection[1].y, projection[2].z) * viewPosition + projection[3].xyz
            """, """
            ($1 * vec4($2$3, 1.0)).xyz""",
        "(?:diagonal3|diag3|vec3)\\((\\w+)(?:\\[0]\\.x,\\1\\[1]\\.y,\\1\\[2]\\.z)?\\)\\*(?:(\\w+)|\\((\\w+)\\))\\+(?:\\(\\1\\)|\\1)\\[3]\\.xyz",
        "\\((?:diagonal3|diag3|vec3)\\((\\w+)(?:\\[0]\\.x,\\1\\[1]\\.y,\\1\\[2]\\.z)?\\)\\*(?:(\\w+)|\\((\\w+)\\))\\)\\+(?:\\(\\1\\)|\\1)\\[3]\\.xyz");

    private static final ShaderPatch PROJECT_PREVIOUS_TO_SCREEN = ShaderPatch.of("previous-frame: screen via diagonal", """
            final_pos = vec2(gbufferPreviousProjection[0].x, gbufferPreviousProjection[1].y) * prev_view_pos.xy + gbufferPreviousProjection[3].xy;
            texcoord_past = (final_pos / -prev_view_pos.z) * 0.5 + 0.5;""", """
            vec4 visorScreenPos = $2 * vec4($3, 1.0);
            $1 = visorScreenPos.xy;
            $4 = visorScreenPos.xy / visorScreenPos.w * 0.5 + 0.5;
            """,
        "(\\w+)=vec2\\((\\w+)\\[0]\\.x,\\2\\[1]\\.y\\)\\*(\\w+)\\.xy\\+\\2\\[3]\\.xy;(\\w+)=\\(\\1/-\\3\\.z\\)\\*0\\.5\\+0\\.5;");

    /** active patch table. */
    private static final List<ShaderPatch> PATCHES = List.of(
        REDEFINE_PROJMAD_MACRO,
        PROJECT_VIA_DIAGONAL4_Z,
        PROJECT_VIA_MAT3_SCALE,
        PROJECT_VIA_DIAGONAL4,
        PROJECT_VIA_PROJMAD,
        UNPROJECT_VIA_DIAGONAL_DIVIDE,
        UNPROJECT_NDC_VIA_DIAGONAL,
        UNPROJECT_VIA_PROJMAD,
        UNPROJECT_TEXCOORD_VIA_DIAGONAL,
        PROJECT_TO_SCREEN_XY,
        PROJECT_LINE_TO_SCREEN_XY,
        PROJECT_TO_SCREEN_XYZ,
        PROJECT_TO_SCREEN_VIA_DIAGONAL2,
        PROJECT_VIA_DIAGONAL3,
        PROJECT_PREVIOUS_TO_SCREEN
    );

    /**
     * Names of the rules that have changed at least one shader source. Lets a diagnostic
     * answer "which projection shortcuts did this pack actually use" without per-source log spam; read
     * via {@link #firedRules()}
     */
    private static final Set<String> FIRED_RULES = ConcurrentHashMap.newKeySet();

    /** How many shader sources the patcher has actually changed (read via {@link #patchedSourceCount()}) */
    private static final AtomicInteger PATCHED_SOURCES = new AtomicInteger();

    /** Whether we have already logged that the patcher changed a shader source */
    private static volatile boolean loggedFirstPatch = false;

    private ShaderPatcher() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Patches known VR incompatibilities out of a shader source
     *
     * @param shader shader source to patch
     * @return the patched source (or the original, unchanged, if patching fails)
     */
    public static String patchShader(String shader) {
        if (shader == null){
            return shader;
        }
        final String original = shader;
        List<String> firedHere = null;
        try {
            for (ShaderPatch patch : PATCHES) {
                String before = shader;
                shader = patch.applyTo(shader);
                if (!before.equals(shader)) {
                    if (firedHere == null) {
                        firedHere = new ArrayList<>(4);
                    }
                    firedHere.add(patch.name());
                    FIRED_RULES.add(patch.name());
                }
            }
        } catch (Throwable t) {
            return original;
        }
        if (firedHere != null) {
            PATCHED_SOURCES.incrementAndGet();
            if (!loggedFirstPatch) {
                loggedFirstPatch = true;
            }
        }
        return shader;
    }

    /** @return the active patch table */
    public static List<ShaderPatch> patches() {
        return PATCHES;
    }

    /**
     * @return the {@link ShaderPatch#name() names} of the rules that have changed at least one shader
     */
    public static Set<String> firedRules() {
        return Set.copyOf(FIRED_RULES);
    }

    /** @return how many shader sources the patcher has actually changed */
    public static int patchedSourceCount() {
        return PATCHED_SOURCES.get();
    }
}
