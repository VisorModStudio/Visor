package me.phoenixra.visor.core.client.exceptions;

import me.phoenixra.visor.api.common.utils.LoggerUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class VRInitException extends VRException {

    public VRInitException(@NotNull Component title, @NotNull Component error) {
        super(title,error);
        this.title = title;
        this.error = error;
    }
    public VRInitException(@NotNull Component title, @NotNull Component error,
                           Throwable cause) {
        super(title, error, cause);
        this.title = title;
        this.error = error;
    }
    public VRInitException(Throwable cause) {
        super(cause);
        if(cause.getMessage() != null){
            this.title = Component.literal("VRInitError: "+cause.getMessage());
        }else {
            this.title = Component.literal("VRInitError: " + cause.getClass().getName());
        }
        this.error =  LoggerUtils.throwableToComponent(cause);
    }
}
