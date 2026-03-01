package org.vmstudio.visor.core.client.exceptions;

import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class VisorException extends VRException {

    public VisorException(@NotNull Component title, @NotNull Component error) {
        super(title,error);
        this.title = title;
        this.error = error;
    }
    public VisorException(@NotNull Component title, @NotNull Component error,
                          Throwable cause) {
        super(title, error, cause);
        this.title = title;
        this.error = error;
    }
    public VisorException(Throwable cause) {
        super(cause);
        if(cause.getMessage() != null){
            this.title = Component.literal("VisorException: " + cause.getMessage());
        }else {
            this.title = Component.literal("VisorException: " + cause.getClass().getName());
        }
        this.error =  LoggerUtils.throwableToComponent(cause);
    }
}
