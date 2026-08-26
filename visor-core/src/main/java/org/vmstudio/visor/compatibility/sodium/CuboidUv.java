package org.vmstudio.visor.compatibility.sodium;

public record CuboidUv(
        float u0, float u1, float u2, float u3, float u4, float u5,
        float v0, float v1, float v2) {

    public FaceUv face(int sodiumFace) {
        return switch (sodiumFace) {
            case 1 -> new FaceUv(u2, v1, u3, v0);
            case 2 -> new FaceUv(u1, v1, u2, v2);
            case 3 -> new FaceUv(u4, v1, u5, v2);
            case 4 -> new FaceUv(u2, v1, u4, v2);
            case 5 -> new FaceUv(u0, v1, u1, v2);
            default -> new FaceUv(u1, v0, u2, v1);
        };
    }
}