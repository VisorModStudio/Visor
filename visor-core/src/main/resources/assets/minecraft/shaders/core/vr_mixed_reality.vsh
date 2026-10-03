#version 150
#ifdef VISOR_SPIRV
#extension GL_ARB_separate_shader_objects : require
#extension GL_ARB_explicit_attrib_location : require
#define VISOR_LOCATION(n) layout(location = n)
#else
#define VISOR_LOCATION(n)
#endif

VISOR_LOCATION(0) in vec3 Position;
VISOR_LOCATION(1) in vec2 UV0;

VISOR_LOCATION(0) out vec2 texCoord0;

void main() {
    gl_Position = vec4(Position, 1.0);
    texCoord0 = UV0;
}
