#version 150 core

uniform sampler2D Sampler0;

uniform float uTintRed;
uniform float uTintBlue;
uniform float uTintBlack;


in vec2 texCoord0;
out vec4 fragColor;

vec4 applyTints(vec4 col) {
    float red = clamp(uTintRed, 0.0, 1.0);
    float blue = clamp(uTintBlue, 0.0, 1.0);
    float black = clamp(uTintBlack, 0.0, 1.0);
    col.gb *= 1.0 - red;
    col.rg *= vec2(1.0 - blue, 1.0 - 0.5 * blue);
    col.rgb *= 1.0 - black;
    return col;
}

void main(){

    vec4 color = texture(Sampler0, texCoord0.st);

    // --- Apply all tints
    color = applyTints(color);

    // --- Finalize
    fragColor = color;

}
