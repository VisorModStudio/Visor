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

uniform float uDesaturate;


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

vec4 applyDesaturation(vec4 col) {
    float amount = clamp(uDesaturate, 0.0, 1.0);
    float luma = dot(col.rgb, vec3(0.2126, 0.7152, 0.0722));
    col.rgb = mix(col.rgb, vec3(luma), amount);
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

    vec4 color = texture(Sampler0, texCoord0.st);

    // --- Apply all tints
    color = applyTints(color);

    // --- Apply vignette
    float mask = vignetteMask(texCoord0);
    color = mix(color, uVignetteColor, mask);

    // --- Drain the colors
    color = applyDesaturation(color);

    // --- Finalize
    fragColor = color;

}



