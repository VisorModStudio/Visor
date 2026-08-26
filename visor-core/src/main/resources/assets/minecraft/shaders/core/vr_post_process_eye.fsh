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


in vec2 texCoordinates;
out vec4 fragColor;

vec4 applyTints(vec4 col) {
    col.gb *= 1.0 - uTintRed;
    col.rg *= vec2(1.0 - uTintBlue, 1.0 - 0.5 * uTintBlue);
    col.rgb *= 1.0 - uTintBlack;
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



