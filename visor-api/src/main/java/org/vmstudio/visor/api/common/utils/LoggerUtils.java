package org.vmstudio.visor.api.common.utils;

import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryManagerMXBean;

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

    public static Component throwableToComponent(Throwable throwable) {
        String title = throwable.getClass().getName() +
                (throwable.getMessage() == null ? "" : ": " + throwable.getMessage());

        MutableComponent result = Component.literal(title);
        if(throwable.getCause() != null) {
            result.append("Caused by:");
            result.append(throwable.getCause().toString());
        }else{
            for (StackTraceElement element : throwable.getStackTrace()) {
                result.append(Component.literal("\n" + element.toString()));
            }
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
