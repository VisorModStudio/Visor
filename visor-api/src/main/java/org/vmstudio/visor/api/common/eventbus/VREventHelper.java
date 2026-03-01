package org.vmstudio.visor.api.common.eventbus;

import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.common.eventbus.event.VREventCancelable;
import org.vmstudio.visor.api.common.eventbus.event.VREventHasResult;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VREventHelper {

    private static final Map<Class<?>, Boolean> resultCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Boolean> cancelableCache = new ConcurrentHashMap<>();




    public static boolean isCancelable(Class<?> eventClass) {
        return hasAnnotation(eventClass, VREventCancelable.class, cancelableCache);
    }
    public static boolean hasResult(Class<?> eventClass) {
        return hasAnnotation(eventClass, VREventHasResult.class, resultCache);
    }

    private static boolean hasAnnotation(Class<?> eventClass,
                                         Class<? extends Annotation> annotation,
                                         Map<Class<?>, Boolean> cacheMap) {
        if (eventClass == VREvent.class) {
            return false;
        }

        return cacheMap.computeIfAbsent(eventClass, (it) -> {
            var parent = eventClass.getSuperclass();
            return eventClass.isAnnotationPresent(annotation)
                    || (parent != null && hasAnnotation(parent, annotation, cacheMap));
        });
    }
}
