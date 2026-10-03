#version 150 core
#ifdef VISOR_SPIRV
#extension GL_ARB_separate_shader_objects : require
#extension GL_ARB_explicit_attrib_location : require
#define VISOR_LOCATION(n) layout(location = n)
#else
#define VISOR_LOCATION(n)
#endif

#ifdef VISOR_UBO
// the json every older node reads gives this block its member order
layout(std140) uniform VisorUniforms {
    float uInBlockProximity;
};
#else
uniform float uInBlockProximity;
#endif

VISOR_LOCATION(0) in vec2 texCoord0;
VISOR_LOCATION(0) out vec4 fragColor;

void main() {
    vec2 center = texCoord0 - vec2(0.5, 0.5);
    float d = length(center);

    float visibleRadius = mix(1.2, -0.5, uInBlockProximity);
    float softness = mix(0.15, 0.5, uInBlockProximity);
    float darkness = smoothstep(visibleRadius - softness, visibleRadius + softness, d);

    darkness = max(darkness, smoothstep(0.85, 1.0, uInBlockProximity));

    fragColor = vec4(0.0, 0.0, 0.0, darkness);
}