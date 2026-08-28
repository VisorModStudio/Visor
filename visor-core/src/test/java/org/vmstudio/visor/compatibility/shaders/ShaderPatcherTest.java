package org.vmstudio.visor.compatibility.shaders;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ShaderPatcherTest {

    private static void patchesTo(String expected, String input) {
        assertEquals(expected, ShaderPatcher.patchShader(input));
    }

    private static void unchanged(String input) {
        assertEquals(input, ShaderPatcher.patchShader(input));
    }

    // ---- macro definition ----

    @Test
    void projMadMacroDefinition() {
        patchesTo(
                "#define projMAD(m, v) (((m) * vec4((v), 1.0)).xyz)",
                "#define projMAD(m, v) (diag3(m) * (v) + (m)[3].xyz)");
        patchesTo(
                "#define projMAD(mat, pos) (((mat) * vec4((pos), 1.0)).xyz)",
                "#define projMAD(mat, pos) (diagonal3(mat) * (pos) + (mat)[3].xyz)");
    }

    // ---- view -> clip ----

    @Test
    void diag3Products() {
        patchesTo(
                "probe = (cam2clip * vec4(offset, 1.0)).xyz;",
                "probe = diag3(cam2clip) * offset + cam2clip[3].xyz;");
        // product wrapped in parentheses
        patchesTo(
                "probe = (view2clip * vec4((delta), 1.0)).xyz;",
                "probe = (diagonal3(view2clip) * (delta)) + view2clip[3].xyz;");
        // spelled-out diagonal
        patchesTo(
                "fog = (cam2clip * vec4(eyeDir, 1.0)).xyz;",
                "fog = vec3(cam2clip[0].x, cam2clip[1].y, cam2clip[2].z) * eyeDir + cam2clip[3].xyz;");
        // rgba swizzle aliases
        patchesTo(
                "glow = (m * vec4(v, 1.0)).xyz;",
                "glow = diag3(m) * v + m[3].rgb;");
    }

    @Test
    void diag4XyzzProducts() {
        patchesTo(
                "hcoord = cam2clip * vec4(wsDelta, 1.0);",
                "hcoord = wsDelta.xyzz * diagonal4(cam2clip) + cam2clip[3];");
        // premultiplied diagonal held in its own variable
        patchesTo(
                "view = clip2view * vec4(frag, 1.0);",
                "view = invDiag * frag.xyzz + clip2view[3];");
        // spelled-out diagonal
        patchesTo(
                "view = clip2view * vec4(ndc, 1.0);",
                "view = vec4(clip2view[0].x, clip2view[1].y, clip2view[2].zw) * ndc.xyzz + clip2view[3];");
        // diagonal call on the left of the product
        patchesTo(
                "hpos = cam2clip * vec4(wsPos, 1.0);",
                "hpos = diag4(cam2clip) * wsPos.xyzz + cam2clip[3];");
        // rgba swizzle aliases
        patchesTo(
                "sum = clipInv * vec4(probe, 1.0);",
                "sum = probe.rgbb * diag4(clipInv) + clipInv[3];");
        // xyzw is not the doubled-z idiom
        unchanged("far = probe.xyzw * diag4(m) + m[3];");
    }

    @Test
    void diag4WithExplicitZColumn() {
        patchesTo(
                "hpos = cam2clip * vec4(hpos.xyz, 1.0);",
                "hpos = hpos.xyzz * diag4(cam2clip) + vec4(0.0, 0.0, cam2clip[3].z, 0.0);");
        // tolerated number spellings
        patchesTo(
                "hpos = proj * vec4(hpos.xyz, 1.0);",
                "hpos = hpos.xyzz * diagonal4(proj) + vec4(0., 0, proj[3].z, 0.0);");
        // different variable on each side is a different idiom
        unchanged("outp = inp.xyzz * diag4(m) + vec4(0.0, 0.0, m[3].z, 0.0);");
    }

    @Test
    void mat3ScaleAssembly() {
        patchesTo(
                "gl_Position = cam2clip * vec4(eyePos, 1.0);",
                """
                gl_Position.xyz = scaleOf(mat3(cam2clip)) * eyePos;
                gl_Position.z += cam2clip[3].z;
                gl_Position.w = -eyePos.z;""");
    }

    @Test
    void projMadWithWTerm() {
        patchesTo(
                "vec4 clipPos = cam2clip * vec4(eyePos, 1.0);",
                "vec4 clipPos = vec4(applyMAD(cam2clip, eyePos), eyePos.z * cam2clip[2].w);");
    }

    // ---- screen -> view ----

    @Test
    void unprojectDiagThenDivide() {
        patchesTo(
                """
                vec4 eyeRayVisor = clip2view * vec4(winUV.xyz, 1.0);
                vec3 eyeRay = eyeRayVisor.xyz / eyeRayVisor.w;""",
                """
                vec3 eyeRay = vec3(vec2(clip2view[0].x, clip2view[1].y) * winUV.xy + clip2view[3].xy, clip2view[3].z);
                eyeRay /= clip2view[2].w * winUV.z + clip2view[3].w;""");
    }

    @Test
    void unprojectNdcDiag() {
        patchesTo(
                """
                vec4 ray = clipInv * vec4(coord * 2.0 - 1.0, 1.0);
                return ray.xyz / ray.w;""",
                """
                vec3 ray = vec3(vec2(clipInv[0].x, clipInv[1].y) * (coord.xy * 2.0 - 1.0), -1.0);
                return ray / (clipInv[2].w * (coord.z * 2.0 - 1.0) + clipInv[3].w);""");
        // distributed multiplication variant
        patchesTo(
                """
                vec4 ray = clipInv * vec4(coord * 2.0 - 1.0, 1.0);
                return ray.xyz / ray.w;""",
                """
                vec3 ray = vec3(vec2(clipInv[0].x, clipInv[1].y) * coord.xy * 2.0 - vec2(clipInv[0].x, clipInv[1].y), -1.0);
                return ray / (clipInv[2].w * coord.z * 2.0 - clipInv[2].w + clipInv[3].w);""");
    }

    @Test
    void unprojectMacroCall() {
        patchesTo(
                """
                vec4 srcPosVisor = clip2view * vec4(srcPos, 1.0);
                return srcPosVisor.xyz / srcPosVisor.w;""",
                "return unproj(clip2view, srcPos) / (srcPos.z * clip2view[2].w + clip2view[3].w);");
    }

    @Test
    void unprojectTexcoordDepthPair() {
        patchesTo(
                """
                vec4 eyePointVisor = clip2view * vec4(vec3(texUV, sceneDepth) * 2.0 - 1.0, 1.0);
                eyePoint = eyePointVisor.xyz / eyePointVisor.w;""",
                """
                eyePoint = vec3(vec2(clip2view[0].x, clip2view[1].y) * (texUV * 2.0 - 1.0) + clip2view[3].xy, clip2view[3].z);
                eyePoint /= (clip2view[2].w * (sceneDepth * 2.0 - 1.0) + clip2view[3].w);""");
    }

    // ---- view -> screen uv ----

    @Test
    void screenUvFromDiag() {
        patchesTo(
                """
                vec4 winXY = cam2clip * vec4(eyePos, 1.0);
                return (winXY.xy / winXY.w) * 0.5 + 0.5;""",
                """
                vec2 winXY = vec2(cam2clip[0].x, cam2clip[1].y) * eyePos.xy;
                return 0.5 - (winXY.xy / eyePos.z) * 0.5;""");
        // division spelling of the half scale
        patchesTo(
                """
                vec4 winXY = cam2clip * vec4(eyePos, 1.0);
                return (winXY.xy / winXY.w) * 0.5 + 0.5;""",
                """
                vec2 winXY = vec2(cam2clip[0].x, cam2clip[1].y) * eyePos.xy;
                return 0.5 - (winXY.xy / eyePos.z) / 2.0;""");
    }

    @Test
    void screenUvFromDiag2Call() {
        patchesTo(
                """
                vec4 eyePosVisor = cam2clip * vec4(eyePos, 1.0);
                return (eyePosVisor.xy / eyePosVisor.w) * 0.5 + 0.5;""",
                "return (diag2(cam2clip) * eyePos.xy + cam2clip[3].xy) / -eyePos.z * 0.5 + 0.5;");
    }

    @Test
    void screenUvzFromDiag() {
        patchesTo(
                """
                vec4 winXY = cam2clip * vec4(eyePos, 1.0);
                return (winXY.xyz / winXY.w) * 0.5 + 0.5;""",
                """
                vec2 winXY = vec2(cam2clip[0].x, cam2clip[1].y) * eyePos.xy;
                return 0.5 - vec3(winXY.xy / eyePos.z, cam2clip[3].z / eyePos.z + cam2clip[2].z) * 0.5;""");
    }

    @Test
    void screenLinePairWithDepthScale() {
        patchesTo(
                """
                vec2 clipA = (cam2clip * vec4(segStart, 1.0)).xy * 0.99609375;
                vec2 clipB = (cam2clip * vec4(segEnd, 1.0)).xy * 0.99609375;
                vec3 middle = cross(vec3(clipA, 0.0), vec3(clipB, 0.0));
                float segDepth = segStart.z * 0.99609375;""",
                """
                vec2 clipA = vec2(cam2clip[0].x, cam2clip[1].y) * segStart.xy;
                vec2 clipB = vec2(cam2clip[0].x, cam2clip[1].y) * segEnd.xy;
                vec3 middle = cross(vec3(clipA, 0.0), vec3(clipB, 0.0));
                float segDepth = segStart.z * 0.99609375;""");
    }

    @Test
    void screenUvPreviousFrame() {
        patchesTo(
                """
                vec4 prevEyeVisor = prevCam2clip * vec4(prevEye, 1.0);
                histClip = prevEyeVisor.xy;
                histUV = prevEyeVisor.xy / prevEyeVisor.w * 0.5 + 0.5;""",
                """
                histClip = vec2(prevCam2clip[0].x, prevCam2clip[1].y) * prevEye.xy + prevCam2clip[3].xy;
                histUV = (histClip / -prevEye.z) * 0.5 + 0.5;""");
    }

    // ---- non-matches ----

    @Test
    void lookalikesStayUntouched() {
        unchanged("shadowPos = diag3(shadowProj) * worldPos;");
        unchanged("p = q.xyzz * diag4(m);");
        unchanged("uv = vec2(m[1].x, m[0].y) * v.xy;");
        unchanged("color = texture(sampler, coord).rgb * tint;");
    }

    @Test
    void mixedSourcePatchesEachIdiomOnce() {
        patchesTo(
                """
                uniform mat4 cam2clip;
                #define projMAD(m, v) (((m) * vec4((v), 1.0)).xyz)
                vec3 toView(vec3 winUV) {
                vec4 winPosVisor = clip2view * vec4(winUV.xyz, 1.0);
                vec3 winPos = winPosVisor.xyz / winPosVisor.w;
                return winPos;
                }
                shadowPos = diag3(shadowProj) * worldPos;
                hcoord = cam2clip * vec4(wsDelta, 1.0);""",
                """
                uniform mat4 cam2clip;
                #define projMAD(m, v) (diag3(m) * (v) + (m)[3].xyz)
                vec3 toView(vec3 winUV) {
                vec3 winPos = vec3(vec2(clip2view[0].x, clip2view[1].y) * winUV.xy + clip2view[3].xy, clip2view[3].z);
                winPos /= clip2view[2].w * winUV.z + clip2view[3].w;
                return winPos;
                }
                shadowPos = diag3(shadowProj) * worldPos;
                hcoord = wsDelta.xyzz * diagonal4(cam2clip) + cam2clip[3];""");
    }
}
