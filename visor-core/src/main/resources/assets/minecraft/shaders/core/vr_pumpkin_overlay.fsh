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
    float uOpacity;
};
#else
uniform float uOpacity;
#endif

VISOR_LOCATION(0) in vec2 texCoord0;
VISOR_LOCATION(0) out vec4 fragColor;

void main() {
    vec2 halfTexel = 0.5 / vec2(textureSize(Sampler0, 0));
    vec4 color = texture(Sampler0, clamp(texCoord0, halfTexel, 1.0 - halfTexel));

    fragColor = vec4(color.rgb, color.a * uOpacity);
}
