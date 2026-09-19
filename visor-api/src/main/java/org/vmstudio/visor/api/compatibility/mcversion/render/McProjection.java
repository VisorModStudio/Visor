package org.vmstudio.visor.api.compatibility.mcversion.render;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
//? if >=1.21.2 {
import com.mojang.blaze3d.ProjectionType;
//?} else {
/*import com.mojang.blaze3d.vertex.VertexSorting;
*///?}
//? if >=1.21.6 {
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;
//?}

/**
 * Cross-mc-version facade over the projection matrix
 */
public class McProjection {
    private McProjection() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    //? if >=1.21.6 {
    private static final int SLOTS = 16;
    private static final McUniformRing RING = new McUniformRing("visor projection", 64, SLOTS);
    private static final Matrix4f[] UPLOADED = new Matrix4f[SLOTS];

    private static void set(Matrix4fc projection, ProjectionType type) {
        RenderSystem.setProjectionMatrix(upload(projection), type);
    }

    static void setKeepingType(Matrix4fc projection) {
        set(projection, RenderSystem.getProjectionType());
    }

    static GpuBufferSlice upload(Matrix4fc projection) {
        GpuBufferSlice slice;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            slice = RING.write(Std140Builder.onStack(stack, 64).putMat4f(projection).get());
        }
        int slot = RING.slot(slice);
        if (UPLOADED[slot] == null) {
            UPLOADED[slot] = new Matrix4f();
        }
        UPLOADED[slot].set(projection);
        return slice;
    }
    //?}

    public static void setPerspective(Matrix4f projection) {
        //? if >=1.21.6 {
        set(projection, ProjectionType.PERSPECTIVE);
        //?} elif >=1.21.2 {
        /*RenderSystem.setProjectionMatrix(projection, ProjectionType.PERSPECTIVE);
        *///?} else {
        /*RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);
        *///?}
    }

    public static void setOrthographic(Matrix4f projection) {
        //? if >=1.21.6 {
        set(projection, ProjectionType.ORTHOGRAPHIC);
        //?} elif >=1.21.2 {
        /*RenderSystem.setProjectionMatrix(projection, ProjectionType.ORTHOGRAPHIC);
        *///?} else {
        /*RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);
        *///?}
    }

    public static State save() {
        //? if >=1.21.6 {
        GpuBufferSlice slice = RenderSystem.getProjectionMatrixBuffer();
        int slot = slice == null ? -1 : RING.slot(slice);
        // a ring slot is overwritten by later uploads, so a Visor projection is restored from its copy
        return new State(slice, RenderSystem.getProjectionType(),
                slot < 0 ? null : new Matrix4f(UPLOADED[slot]));
        //?} elif >=1.21.2 {
        /*return new State(new Matrix4f(RenderSystem.getProjectionMatrix()), RenderSystem.getProjectionType());
        *///?} else {
        /*return new State(new Matrix4f(RenderSystem.getProjectionMatrix()), RenderSystem.getVertexSorting());
        *///?}
    }

    public static void restore(State state) {
        //? if >=1.21.6 {
        if (state.matrix != null) {
            set(state.matrix, state.type);
        } else {
            RenderSystem.setProjectionMatrix(state.slice, state.type);
        }
        //?} else {
        /*RenderSystem.setProjectionMatrix(state.matrix, state.type);
        *///?}
    }

    public static final class State {
        private final Matrix4f matrix;
        //? if >=1.21.6 {
        private final GpuBufferSlice slice;
        private final ProjectionType type;

        private State(GpuBufferSlice slice, ProjectionType type, Matrix4f matrix) {
            this.slice = slice;
            this.type = type;
            this.matrix = matrix;
        }
        //?} elif >=1.21.2 {
        /*private final ProjectionType type;

        private State(Matrix4f matrix, ProjectionType type) {
            this.matrix = matrix;
            this.type = type;
        }
        *///?} else {
        /*private final VertexSorting type;

        private State(Matrix4f matrix, VertexSorting type) {
            this.matrix = matrix;
            this.type = type;
        }
        *///?}
    }
}
