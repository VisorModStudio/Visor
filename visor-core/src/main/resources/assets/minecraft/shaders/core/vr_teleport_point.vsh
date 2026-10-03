#version 330 core
#ifdef VISOR_SPIRV
#extension GL_ARB_separate_shader_objects : require
#extension GL_ARB_explicit_attrib_location : require
#define VISOR_LOCATION(n) layout(location = n)
#define VISOR_VERTEX_ID gl_VertexIndex
#else
#define VISOR_LOCATION(n)
#define VISOR_VERTEX_ID gl_VertexID
#endif


VISOR_LOCATION(0) in vec3 Position;


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

VISOR_LOCATION(0) out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    if (VISOR_VERTEX_ID == 0)
    texCoord0 = vec2(0.0, 0.0);
    else if (VISOR_VERTEX_ID == 1)
    texCoord0 = vec2(1.0, 0.0);
    else if (VISOR_VERTEX_ID == 2)
    texCoord0 = vec2(1.0, 1.0);
    else if (VISOR_VERTEX_ID == 3)
    texCoord0 = vec2(0.0, 1.0);
}