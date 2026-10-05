#version 150 core
#ifdef VISOR_SPIRV
#extension GL_ARB_separate_shader_objects : require
#extension GL_ARB_explicit_attrib_location : require
#define VISOR_LOCATION(n) layout(location = n)
#else
#define VISOR_LOCATION(n)
#endif

uniform sampler2D Sampler0;

#ifdef VISOR_UBO
// the json every older node reads gives this block its member order
layout(std140) uniform VisorUniforms {
    float uTintRed;
    float uTintBlue;
    float uTintBlack;
    float uDesaturate;
    float uScalingAxis;
    float uScalingFilter;
    float uSourceWidth;
    float uSourceHeight;
    float uTargetWidth;
    float uTargetHeight;
};
#else
uniform float uTintRed;
uniform float uTintBlue;
uniform float uTintBlack;

uniform float uDesaturate;

uniform float uScalingAxis;
uniform float uScalingFilter;
uniform float uSourceWidth;
uniform float uSourceHeight;
uniform float uTargetWidth;
uniform float uTargetHeight;
#endif

VISOR_LOCATION(0) in vec2 texCoord0;
VISOR_LOCATION(0) out vec4 fragColor;

const float PI = 3.14159265;
const int MAX_TAPS = 64;

float filterWeight(float x) {
    x = abs(x);
    if (uScalingFilter < 0.5) {
        if (x < 1.0e-4) {
            return 1.0;
        }
        if (x >= 2.0) {
            return 0.0;
        }
        float px = PI * x;
        return 2.0 * sin(px) * sin(0.5 * px) / (px * px);
    }
    if (uScalingFilter < 1.5) {
        if (x < 1.0) {
            return (7.0 * x * x * x - 12.0 * x * x + 16.0 / 3.0) / 6.0;
        }
        if (x < 2.0) {
            return (-7.0 / 3.0 * x * x * x + 12.0 * x * x - 20.0 * x + 32.0 / 3.0) / 6.0;
        }
        return 0.0;
    }
    return max(1.0 - x, 0.0);
}

vec4 resample(bool horizontal) {
    vec2 targetPos = texCoord0 * vec2(uTargetWidth, uTargetHeight);
    float sourceSize = horizontal ? uSourceWidth : uSourceHeight;
    float scale = sourceSize / (horizontal ? uTargetWidth : uTargetHeight);
    float stretch = max(scale, 1.0);
    float center = (horizontal ? targetPos.x : targetPos.y) * scale - 0.5;
    float radius = (uScalingFilter < 1.5 ? 2.0 : 1.0) * stretch;

    int first = int(ceil(center - radius));
    int last = min(int(floor(center + radius)), first + MAX_TAPS);
    int line = int(horizontal ? targetPos.y : targetPos.x);
    int lastIndex = int(sourceSize) - 1;

    vec3 sum = vec3(0.0);
    float weightSum = 0.0;
    vec3 lobeMin = vec3(1.0);
    vec3 lobeMax = vec3(0.0);
    for (int i = first; i <= last; i++) {
        float offset = (float(i) - center) / stretch;
        int index = clamp(i, 0, lastIndex);
        vec3 texel = texelFetch(Sampler0, horizontal ? ivec2(index, line) : ivec2(line, index), 0).rgb;
        float weight = filterWeight(offset);
        sum += texel * weight;
        weightSum += weight;
        if (abs(offset) < 1.0) {
            lobeMin = min(lobeMin, texel);
            lobeMax = max(lobeMax, texel);
        }
    }
    vec3 color = sum / max(weightSum, 1.0e-4);
    if (uScalingFilter < 0.5) {
        // anti-ringing: lanczos lobes may not push past the texels under the main lobe (halos)
        color = clamp(color, lobeMin, lobeMax);
    }
    return vec4(color, 1.0);
}

vec4 applyTints(vec4 col) {
    float red = clamp(uTintRed, 0.0, 1.0);
    float blue = clamp(uTintBlue, 0.0, 1.0);
    float black = clamp(uTintBlack, 0.0, 1.0);
    col.gb *= 1.0 - red;
    col.rg *= vec2(1.0 - blue, 1.0 - 0.5 * blue);
    col.rgb *= 1.0 - black;
    return col;
}

vec4 applyDesaturation(vec4 col) {
    float amount = clamp(uDesaturate, 0.0, 1.0);
    float luma = dot(col.rgb, vec3(0.2126, 0.7152, 0.0722));
    col.rgb = mix(col.rgb, vec3(luma), amount);
    return col;
}

void main(){
    vec4 color;
    if (uScalingAxis < 0.5) {
        color = texture(Sampler0, texCoord0.st);
    } else {
        // alpha 1: the json blend of <=1.20.6 must not mix the intermediate target in
        color = resample(uScalingAxis < 1.5);
    }

    // --- Apply all tints
    color = applyTints(color);

    // --- Drain the colors
    color = applyDesaturation(color);

    // --- Finalize
    fragColor = color;

}
