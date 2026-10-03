package org.vmstudio.visor.api.compatibility.mcversion.render;

//? if >=1.21.6 {
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;

import java.nio.ByteBuffer;

final class McUniformRing {
    private final String label;
    private final int blockSize;
    private final int slots;
    private GpuBuffer buffer;
    private int stride;
    private int next;

    McUniformRing(String label, int blockSize, int slots) {
        this.label = label;
        this.blockSize = blockSize;
        this.slots = slots;
    }

    int blockSize() {
        return blockSize;
    }

    //? if >=26.3 {
    private GpuBufferSlice[] written;

    int slot(GpuBufferSlice slice) {
        if (written != null) {
            for (int i = 0; i < slots; i++) {
                if (written[i] == slice) {
                    return i;
                }
            }
        }
        return -1;
    }

    GpuBufferSlice write(ByteBuffer data) {
        if (written == null) {
            written = new GpuBufferSlice[slots];
        }
        int alignment = Math.max(1, RenderSystem.getDevice().getDeviceInfo().limits().minUniformOffsetAlignment());
        GpuBufferSlice slice = RenderSystem.getDevice().createCommandEncoder().transientMemory()
                .uploadGpu(data, alignment, GpuBuffer.USAGE_UNIFORM);
        written[next] = slice;
        next = (next + 1) % slots;
        return slice;
    }
    //?} elif >=26.2 {
    /*int slot(GpuBufferSlice slice) {
        // offset() is a long since 1.21.11
        return buffer != null && slice.buffer() == buffer ? (int) (slice.offset() / stride) : -1;
    }

    GpuBufferSlice write(ByteBuffer data) {
        if (buffer == null) {
            int alignment = Math.max(1, RenderSystem.getDevice().getDeviceInfo().limits().minUniformOffsetAlignment());
            stride = (blockSize + alignment - 1) / alignment * alignment;
            buffer = RenderSystem.getDevice().createBuffer(() -> label,
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, stride * slots);
        }
        GpuBufferSlice slice = buffer.slice(next * stride, blockSize);
        next = (next + 1) % slots;
        RenderSystem.getDevice().createCommandEncoder().writeToBuffer(slice, data);
        return slice;
    }
    *///?} else {
    /*int slot(GpuBufferSlice slice) {
        // offset() is a long since 1.21.11
        return buffer != null && slice.buffer() == buffer ? (int) (slice.offset() / stride) : -1;
    }

    GpuBufferSlice write(ByteBuffer data) {
        if (buffer == null) {
            int alignment = Math.max(1, RenderSystem.getDevice().getUniformOffsetAlignment());
            stride = (blockSize + alignment - 1) / alignment * alignment;
            buffer = RenderSystem.getDevice().createBuffer(() -> label,
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, stride * slots);
        }
        GpuBufferSlice slice = buffer.slice(next * stride, blockSize);
        next = (next + 1) % slots;
        RenderSystem.getDevice().createCommandEncoder().writeToBuffer(slice, data);
        return slice;
    }
    *///?}
}
//?} else {
/*final class McUniformRing {
}
*///?}
