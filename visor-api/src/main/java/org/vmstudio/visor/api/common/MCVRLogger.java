package org.vmstudio.visor.api.common;

import lombok.AllArgsConstructor;
import me.phoenixra.atumvr.api.VRLogger;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class MCVRLogger implements VRLogger {
    private final Logger logger;
    private boolean debug;

    public MCVRLogger(@NotNull Logger logger){
        this.logger = logger;
    }

    @Override
    public void logDebug(@NotNull String msg) {
        if(debug) {
            logger.info(msg);
        }
    }

    @Override
    public @NotNull VRLogger setDebug(boolean flag) {
        debug = flag;
        return this;
    }

    @Override
    public void logInfo(@NotNull String msg) {
        logger.info(msg);
    }

    @Override
    public void logWarn(@NotNull String msg) {
        logger.info(msg);
    }

    @Override
    public void logError(@NotNull String msg) {
        logger.info(msg);
    }
}
