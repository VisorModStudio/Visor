#version 150

#moj_import <projection.glsl>

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 IViewRotMat;

out vec3 pos;

void main() {
gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

pos = IViewRotMat * Position;
}