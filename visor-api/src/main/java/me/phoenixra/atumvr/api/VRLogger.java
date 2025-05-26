package me.phoenixra.atumvr.api;


import me.phoenixra.atumconfig.api.ConfigLogger;
import org.jetbrains.annotations.NotNull;


public interface VRLogger extends ConfigLogger {
    /**
     * VR logger with no output
     */
    VRLogger EMPTY = new VRLogger() {
        @Override
        public void logDebug(@NotNull String msg) {}
        @Override
        public void logInfo(@NotNull String msg) {}
        @Override
        public void logWarn(@NotNull String msg) {}
        @Override
        public void logError(@NotNull String msg) {}
        @Override
        public @NotNull VRLogger setDebug(boolean flag) {return this;}
    };

    /**
     * Simple VR logger, that uses System.out.println
     */
    VRLogger SIMPLE = new VRLogger() {
        private boolean debug;
        @Override
        public void logDebug(@NotNull String msg) {if(debug) System.out.println("DEBUG: "+msg);}
        @Override
        public void logInfo(@NotNull String msg) {System.out.println("INFO: "+msg);}
        @Override
        public void logWarn(@NotNull String msg) {System.out.println("WARN: "+msg);}
        @Override
        public void logError(@NotNull String msg) {System.out.println("ERROR: "+msg);}
        @Override
        public @NotNull VRLogger setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }
    };

    /**
     * Logs a debug message.
     *
     * @param msg the message to log, must be non-null
     */
    void logDebug(@NotNull String msg);


    @NotNull
    VRLogger setDebug(boolean flag);

}
