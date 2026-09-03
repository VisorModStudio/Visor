#version 150 core

uniform sampler2D Sampler0;

uniform float uOpacity;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec2 halfTexel = 0.5 / vec2(textureSize(Sampler0, 0));
    vec4 color = texture(Sampler0, clamp(texCoord0, halfTexel, 1.0 - halfTexel));

    fragColor = vec4(color.rgb, color.a * uOpacity);
}
