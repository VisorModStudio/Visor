package org.vmstudio.visor.compatibility.reacharound;

import org.vmstudio.visor.MixinModLoader;
import org.vmstudio.visor.api.client.render.RenderPhase;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;

import java.lang.reflect.Method;

public final class ReacharoundRenderCompat {
    private static final String IRIS_BUFFER_SOURCE =
            "net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource";

    private ReacharoundRenderCompat() {}

    public static void flushPreviewBuffer(Object context) {
        if (!MixinModLoader.get().isModLoaded("connector")
                || !VisorState.get().isActive()
                || VRRenderState.getPhase() != RenderPhase.VR_WORLD) {
            return;
        }

        try {
            Object buffers = invoke(context, "submitNodeCollector", "consumers");
            if (!IRIS_BUFFER_SOURCE.equals(buffers.getClass().getName())) {
                return;
            }

            Object usedSize = invoke(buffers, "getUsedSize");
            if (!(usedSize instanceof Number size) || size.longValue() <= 0L) {
                return;
            }

            invoke(buffers, "endBatch");
        } catch (ReflectiveOperationException ignored) {
            // Fall back to the normal flush if the Connector or Iris API is unavailable.
        }
    }

    private static Object invoke(Object target, String... methodNames) throws ReflectiveOperationException {
        for (String methodName : methodNames) {
            Method method;
            try {
                method = target.getClass().getMethod(methodName);
            } catch (NoSuchMethodException ignored) {
                continue;
            }
            return method.invoke(target);
        }
        throw new NoSuchMethodException(String.join("/", methodNames));
    }
}
