#version 150 core

uniform sampler2D Sampler0;

uniform int uEye = 0;


uniform float uTintRed;
uniform float uTintBlue;
uniform float uTintBlack;

uniform float uVignetteRadius;
uniform float uVignetteOffset = 0.1;
uniform float uVignetteBorder;
uniform vec4 uVignetteColor;


const vec4 BLACK = vec4(0.0, 0.0, 0.0, 1.0);
const float PI = 3.14159265;

in vec2 texCoordinates;
out vec4 fragColor;

vec4 applyTints(vec4 col) {
    if(uTintRed > 0){
        col = mix(col, vec4(col.r, 0.0, 0.0, 1.0), uTintRed);
    }
    if(uTintBlue > 0){
        col = mix(col, vec4(0.0, col.g * 0.5, col.b, 1.0), uTintBlue);
    }
    if(uTintBlack > 0){
        col = mix(col, BLACK, uTintBlack);
    }
    return col;
}

float vignetteMask(vec2 uv) {
    vec2 center = uv - vec2(0.5 + float(uEye) * uVignetteOffset, 0.5);
    float d2 = dot(center, center);
    float inner2 = (uVignetteRadius - uVignetteBorder) * (uVignetteRadius - uVignetteBorder);
    float outer2 = (uVignetteRadius + uVignetteBorder) * (uVignetteRadius + uVignetteBorder);
    return smoothstep(inner2, outer2, d2);
}

void main(){

    vec4 color = texture(Sampler0, texCoordinates.st);

    // --- Apply all tints
    color = applyTints(color);

    // --- Apply vignette
    float mask = vignetteMask(texCoordinates);
    color = mix(color, uVignetteColor, mask);

    // --- Finalize
    fragColor = color;

}



