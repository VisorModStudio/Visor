#version 150 core

uniform float uInBlockProximity;

in vec2 texCoordinates;
out vec4 fragColor;

void main() {
    vec2 center = texCoordinates - vec2(0.5, 0.5);
    float d = length(center);

    float visibleRadius = mix(1.2, -0.5, uInBlockProximity);
    float softness = mix(0.15, 0.5, uInBlockProximity);
    float darkness = smoothstep(visibleRadius - softness, visibleRadius + softness, d);

    darkness = max(darkness, smoothstep(0.85, 1.0, uInBlockProximity));

    fragColor = vec4(0.0, 0.0, 0.0, darkness);
}