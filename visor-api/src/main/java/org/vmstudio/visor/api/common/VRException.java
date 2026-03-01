package org.vmstudio.visor.api.common;

import lombok.Getter;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Getter
public class VRException extends RuntimeException{
    protected Component title;
    protected Component error;

    public VRException(@NotNull Component title, @NotNull Component error) {
        this.title = title;
        this.error = error;
    }
    public VRException(@NotNull Component title, @NotNull Component error,
                           Throwable cause) {
        super(cause);
        this.title = title;
        this.error = error;
    }
    public VRException(Throwable cause) {
        super(cause);
        if(cause.getMessage() != null){
            this.title = Component.literal("VRError: "+cause.getMessage());
        }else {
            this.title = Component.literal("VRError: " + cause.getClass().getName());
        }
        this.error =  LoggerUtils.throwableToComponent(cause);
    }

    public String toString() {
        return this.error.getString();
    }
}
