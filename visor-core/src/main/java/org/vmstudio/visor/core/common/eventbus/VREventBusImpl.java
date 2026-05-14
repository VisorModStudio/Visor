package org.vmstudio.visor.core.common.eventbus;

import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.VREventBus;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.common.eventbus.event.VREventPriority;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VREventBusImpl implements VREventBus {
    private final List<HandlerData> handlers = Collections.synchronizedList(new ArrayList<>());
    private final Map<Class<? extends VREvent>, Map<VREventPriority, List<HandlerData>>> cache = new ConcurrentHashMap<>();

    @Override
    public void callEvent(@NotNull VREvent event) {

        var handlersByPhase = getHandlers(event);


        while (event.nextPhase()) {
            List<HandlerData> handlersForPhase = handlersByPhase
                    .get(event.getPhase());
            if(handlersForPhase == null) continue;

            for (HandlerData handler : handlersForPhase) {
                if (event.isCanceled()
                        && !handler.ignoreCancelled()) {
                    continue;
                }
                try {
                    handler.method().invoke(handler.listener(), event);
                } catch (Throwable e) {
                    LoggerUtils.printError(e);
                }
            }
        }
    }

    @Override
    public void registerListener(@NotNull VisorAddon owner,
                                 @NotNull VREventListener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(VREventHandler.class)) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    throw new IllegalStateException("Method " + method +
                            " in listener " + listener +
                            " is not accessible. It must be declared public.");
                }
                HandlerData data = createHandlerData(owner, listener, method);
                handlers.add(data);
            }
        }
        invalidateCache();
    }

    @Override
    public void unregisterListener(@NotNull VREventListener listener) {
        handlers.removeIf(h -> h.listener().equals(listener));
        invalidateCache();
    }

    @Override
    public void unregisterListeners(@NotNull VisorAddon owner) {
        handlers.removeIf(h -> h.owner().equals(owner));
        invalidateCache();
    }

    @Override
    public void unregisterAllListeners() {
        handlers.clear();
        invalidateCache();
    }

    @Override
    public @NotNull Set<VREventListener> getRegisteredListeners() {
        return handlers.stream()
                .map(HandlerData::listener)
                .collect(Collectors.toSet());
    }

    private void invalidateCache(){
        // I know its not the best solution, but event listeners
        //in most cases are registered during startup,
        // rarely modified in-game, and it is plausible
        // to have thousands of them to noticeably influence on performance
        cache.clear();
    }

    private Map<VREventPriority, List<HandlerData>> getHandlers(@NotNull VREvent event){
        return cache.computeIfAbsent(
                event.getClass(),
                key -> {
                    Map<VREventPriority, List<HandlerData>> map = new EnumMap<>(VREventPriority.class);
                    for (HandlerData h : handlers) {
                        if (h.eventType().isAssignableFrom(key)) {
                            map.computeIfAbsent(h.priority(), k -> new ArrayList<>()).add(h);
                        }
                    }
                    return map;
                }
        );
    }
    private static @NotNull HandlerData createHandlerData(VisorAddon owner,
                                                          VREventListener listener,
                                                          Method method) {
        Class<?>[] params = method.getParameterTypes();
        // Validate that the method has exactly one parameter extending VREvent.
        if (params.length != 1 || !VREvent.class.isAssignableFrom(params[0])) {
            throw new IllegalStateException("Method " + method +
                    " in listener " + listener +
                    " has an invalid signature for event handling. It must have exactly one parameter extending VREvent.");
        }
        VREventHandler annotation = method.getAnnotation(VREventHandler.class);
        method.setAccessible(true);
        return new HandlerData(
                owner,
                listener,
                method,
                params[0],
                annotation.priority(),
                annotation.ignoreCancelled()
        );
    }
}
