#version 330 core


uniform float uTime;
uniform vec3 uColor;


in vec2 texCoordinates;
out vec4 fragColor;


const float maxDist = 0.6;    // Maximum distance in our scaled space.
const float bandWidth = 0.02; // Thickness of each wave band.
const float waveSpeed = 0.15;  // Speed of the wave's outward motion.
const float fadeWidth = 0.3; // The width over which the wave fades out as it nears maxDist.

void main() {
    // Remap UVs from [0,1] to a centered coordinate system.
    // The factor 0.6 controls the overall size of the effect.
    vec2 Scaled = (texCoordinates * 2.0 - vec2(1.0)) * 0.6;
    // Compute a square-like distance (infinity norm) so the effect is square-shaped.
    float SquareDist = max(abs(Scaled.x), abs(Scaled.y));

    // Compute the current wave progress (radial distance) for the first wave.
    float waveProgress1 = mod(uTime * waveSpeed, maxDist);

    float waveProgress2 = mod(uTime * waveSpeed + 0.5 * maxDist, maxDist);

    float waveBand1 = smoothstep(waveProgress1 - bandWidth, waveProgress1, SquareDist)
    - smoothstep(waveProgress1, waveProgress1 + bandWidth, SquareDist);
    float waveBand2 = smoothstep(waveProgress2 - bandWidth, waveProgress2, SquareDist)
    - smoothstep(waveProgress2, waveProgress2 + bandWidth, SquareDist);


    float waveBand = max(waveBand1, waveBand2);

    // Fade the wave's opacity as it approaches the edge.
    float fadeFactor = 1.0 - smoothstep(maxDist - fadeWidth, maxDist, SquareDist);
    waveBand *= fadeFactor;


    //RESULT
    fragColor = vec4(uColor, waveBand);
}