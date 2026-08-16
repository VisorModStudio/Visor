package org.vmstudio.visor.api.common.utils;

import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryManagerMXBean;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class LoggerUtils {

    private LoggerUtils() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static Logger getLogger(){
        if(ModLoader.get().isDedicatedServer()){
            return VisorAPI.server().getLogger();
        }else{
            return VisorAPI.client().getLogger();
        }
    }
    public static void printError(Logger logger, Throwable throwable){
        printError(logger, throwable,
                Collections.newSetFromMap(new IdentityHashMap<>())
        );
    }

    private static void printError(Logger logger, Throwable throwable, Set<Throwable> seen){
        if(!seen.add(throwable)){
            logger.error("[CIRCULAR REFERENCE: {}]", throwable);
            return;
        }
        logger.error(throwable);
        for (StackTraceElement s : throwable.getStackTrace()) {
            logger.error(s.toString());
        }
        for(Throwable err : throwable.getSuppressed()){
            logger.error("Suppressed:");
            printError(logger, err, seen);
        }
        // the cause's own frames are the only thing that says where it came from -
        // printing just its toString() hides the actual origin
        if(throwable.getCause() != null) {
            logger.error("Caused by:");
            printError(logger, throwable.getCause(), seen);
        }
    }
    public static void printError(Throwable throwable){
        printError(getLogger(), throwable);
    }

    public static Component throwableToComponent(Throwable throwable) {
        String title = throwable.getClass().getName() +
                (throwable.getMessage() == null ? "" : ": " + throwable.getMessage());

        MutableComponent result = Component.literal(title);
        for (StackTraceElement element : throwable.getStackTrace()) {
            result.append(Component.literal("\n" + element.toString()));
        }
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            result.append(Component.literal("\nCaused by: "));
            result.append(throwableToComponent(cause));
        }
        return result;
    }

    public static void sendPcInfo(){
        try {
            Logger logger = getLogger();
            String garbageCollector = StringUtils.getCommonPrefix(
                    ManagementFactory
                            .getGarbageCollectorMXBeans()
                            .stream()
                            .map(MemoryManagerMXBean::getName)
                            .toArray(String[]::new)
            ).trim();
            if (garbageCollector.isEmpty()) {
                garbageCollector = ManagementFactory
                        .getGarbageCollectorMXBeans()
                        .get(0)
                        .getName();
            }
            logger.info(
                    "Garbage collector: {}",
                    garbageCollector
            );

            com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

            logger.info(
                    "Available CPU threads: {}",
                    Runtime.getRuntime().availableProcessors()
            );
            logger.info(
                    "Total physical memory: {} GB",
                    String.format(
                            "%.01f",
                            os.getTotalMemorySize() /  1_000_000_000.0F
                    )
            );
            logger.info(
                    "Free physical memory: {} GB",
                    String.format(
                            "%.01f",
                            os.getFreeMemorySize() /  1_000_000_000.0F
                    )
            );

        } catch (Throwable e) {
            LoggerUtils.printError(e);
        }
    }
}
