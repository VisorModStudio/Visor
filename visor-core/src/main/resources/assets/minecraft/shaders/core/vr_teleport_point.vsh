#version 330 core


in vec3 Position;


#ifdef VISOR_UBO
// 1.21.6 feeds the matrices through the engine's std140 blocks, declared like vanilla's gui.vsh does
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
    float LineWidth;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};
#else
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
#endif

out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    if (gl_VertexID == 0)
    texCoord0 = vec2(0.0, 0.0);
    else if (gl_VertexID == 1)
    texCoord0 = vec2(1.0, 0.0);
    else if (gl_VertexID == 2)
    texCoord0 = vec2(1.0, 1.0);
    else if (gl_VertexID == 3)
    texCoord0 = vec2(0.0, 1.0);
}