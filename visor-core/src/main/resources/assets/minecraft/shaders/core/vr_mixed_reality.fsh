#version 330 core

uniform sampler2D SamplerColor;
uniform sampler2D SamplerDepth;


uniform mat4 uInverseProjectionView;

uniform bool uAsGrid2x2;
uniform bool uAlphaMode;

uniform vec3 uHmdViewPosition;
uniform vec3 uHmdPlaneNormal;

uniform vec3 uKeyColor;


in vec2 texCoord0;
out vec4 fragColor;


vec3 nudgeOffKeyColor(in vec3 color) {
    // mask = 1.0 if all |color−keyColor| < ε
    const float eps = 0.004;
    bvec3 close = lessThanEqual(abs(color - uKeyColor), vec3(eps));
    float mask = float(all(close));

    // if keyColor≈black use +eps, otherwise −eps
    vec3 adjust = mix(vec3(-eps), vec3(eps),
                      float(all(lessThanEqual(uKeyColor, vec3(eps)))));

    return color + mask * adjust;
}

vec3 worldPosAt(in vec2 uv) {
    float z = texture(SamplerDepth, uv).r * 2.0 - 1.0;
    vec4 clip = vec4(uv * 2.0 - 1.0, z, 1.0);
    vec4 world = uInverseProjectionView * clip;
    return world.xyz / world.w;
}


void main(void) {

    // default fill = keyColor
    fragColor = vec4(uKeyColor, 1.0);

    if (uAsGrid2x2) {
        // --- 2×2 GRID ---
        vec2 sampleUV = fract(texCoord0 * 2.0);

        if (texCoord0.x < 0.5 && texCoord0.y < 0.5) {
            // bottom-left quadrant = full third-person
            fragColor.rgb = texture(SamplerColor, sampleUV).rgb;

        } else if (texCoord0.y >= 0.5) {
            // top half = front-view pass
            vec3 fragPos = worldPosAt(sampleUV);

            if (dot(fragPos - uHmdViewPosition, uHmdPlaneNormal) >= 0.0) {
                // left-top = color (+ possible key-avoid)
                if (texCoord0.x < 0.5) {
                    vec3 col = texture(SamplerColor, sampleUV).rgb;
                    if (!uAlphaMode) col = nudgeOffKeyColor(col);
                    fragColor.rgb = col;

                } else if (uAlphaMode) {
                    // right-top = white mask
                    fragColor.rgb = vec3(1.0);
                }
            }
        }

    } else {
        // --- SIDE-BY-SIDE LAYOUT ---
        vec2 sampleUV = fract(texCoord0 * vec2(2.0, 1.0));


        if (texCoord0.x >= 0.5) {
            // right half = full third-person
            fragColor.rgb = texture(SamplerColor, sampleUV).rgb;

        } else {
            // left half = front-view + key-avoid
            vec3 fragPos = worldPosAt(sampleUV);
            if (dot(fragPos - uHmdViewPosition, uHmdPlaneNormal) >= 0.0) {
                vec3 col = texture(SamplerColor, sampleUV).rgb;
                fragColor.rgb = nudgeOffKeyColor(col);
            }
        }
    }
}
