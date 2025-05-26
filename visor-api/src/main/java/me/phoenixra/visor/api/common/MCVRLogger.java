package me.phoenixra.visor.api.common;

import lombok.AllArgsConstructor;
import me.phoenixra.atumvr.api.VRLogger;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class MCVRLogger implements VRLogger {
    private final Logger logger;

    @Override
    public void logDebug(@NotNull String msg) {
        logger.info(msg);
    }

    @Override
    public @NotNull VRLogger setDebug(boolean flag) {
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
