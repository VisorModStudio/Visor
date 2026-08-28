package org.vmstudio.visor.api.common.utils;

import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryManagerMXBean;
import java.util.stream.Collectors;

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
        logger.error(throwable);
        for (StackTraceElement s : throwable.getStackTrace()) {
            logger.error(s.toString());
        }
        if(throwable.getCause() != null) {
            logger.error("Caused by:");
            logger.error(throwable.getCause().toString());
        }
        for(Throwable err : throwable.getSuppressed()){
            printError(logger, err);
        }
    }
    public static void printError(Throwable throwable){
        printError(getLogger(), throwable);
    }

    public static Component describeThrowable(Throwable throwable) {
        MutableComponent text = Component.literal(String.valueOf(throwable));
        Throwable cause = throwable.getCause();
        if (cause != null) {
            text.append("\nCaused by: " + cause);
            return text;
        }
        for (StackTraceElement frame : throwable.getStackTrace()) {
            text.append("\n\tat " + frame);
        }
        return text;
    }

    public static void sendPcInfo(){
        try {
            Logger logger = getLogger();
            Runtime runtime = Runtime.getRuntime();

            String collectors = ManagementFactory.getGarbageCollectorMXBeans().stream()
                    .map(MemoryManagerMXBean::getName)
                    .collect(Collectors.joining(", "));
            logger.info("GC in use: {}", collectors.isEmpty() ? "unknown" : collectors);
            logger.info("CPU threads: {}", runtime.availableProcessors());
            logger.info("JVM max heap: {} MiB", runtime.maxMemory() >> 20);

            if (ManagementFactory.getOperatingSystemMXBean()
                    instanceof com.sun.management.OperatingSystemMXBean hostOs) {
                logger.info("Physical memory: {} MiB free of {} MiB",
                        hostOs.getFreeMemorySize() >> 20,
                        hostOs.getTotalMemorySize() >> 20);
            }
        } catch (Throwable e) {
            printError(e);
        }
    }
}
