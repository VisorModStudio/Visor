#version 330 core


in vec3 Position;


uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoordinates;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    if (gl_VertexID == 0)
    texCoordinates = vec2(0.0, 0.0);
    else if (gl_VertexID == 1)
    texCoordinates = vec2(1.0, 0.0);
    else if (gl_VertexID == 2)
    texCoordinates = vec2(1.0, 1.0);
    else if (gl_VertexID == 3)
    texCoordinates = vec2(0.0, 1.0);
}