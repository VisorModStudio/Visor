#version 150
#ifdef VISOR_SPIRV
#extension GL_ARB_separate_shader_objects : require
#extension GL_ARB_explicit_attrib_location : require
#define VISOR_LOCATION(n) layout(location = n)
#else
#define VISOR_LOCATION(n)
#endif

#ifdef VISOR_UBO
// 1.21.6 feeds the matrices through the engine's std140 blocks, declared like vanilla's gui.vsh does
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};
#else
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
#endif

VISOR_LOCATION(0) in vec3 Position;
VISOR_LOCATION(1) in vec2 UV0;

VISOR_LOCATION(0) out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord0 = UV0;
}