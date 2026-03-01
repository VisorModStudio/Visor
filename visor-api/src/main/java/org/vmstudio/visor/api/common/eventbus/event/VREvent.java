package org.vmstudio.visor.api.common.eventbus.event;

import lombok.Getter;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.VREventHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public abstract class VREvent {

    private final VisorAddon owner;

    private boolean canceled;

    private VREventPriority phase = null;

    private Result result = Result.DEFAULT;

    public VREvent(@NotNull VisorAddon owner){
        Objects.requireNonNull(owner);
        this.owner = owner;
    }

    public void requireModifiable(){
        if(phase == VREventPriority.MONITOR){
            throw new UnsupportedOperationException(
                    "Attempted to modify event during phase: " +phase
                            + " Event Type: " + this.getClass().getCanonicalName()
            );
        }
    }


    public final void setCanceled(boolean flag){
        if (!isCancelable())
        {
            throw new UnsupportedOperationException(
                    "Called VREvent#setCanceled() on a non-cancelable event: "
                            + this.getClass().getCanonicalName()
            );
        }
        canceled = flag;
    }

    public boolean isCancelable() {
        return VREventHelper.isCancelable(this.getClass());
    }



    public boolean hasResult() {
        return VREventHelper.hasResult(this.getClass());
    }

    public void setResult(Result flag) {
        if (!hasResult())
        {
            throw new UnsupportedOperationException(
                    "Called VREvent#setResult() on a non-result event: "
                            + this.getClass().getCanonicalName()
            );
        }
        result = flag;
    }


    public boolean nextPhase(){
        if(phase == null){
            phase = VREventPriority.LOWEST;
            return true;
        }
        if(phase == VREventPriority.MONITOR){
            return false;
        }
        phase = phase.next();
        return true;
    }




    public enum Result
    {
        DENY,
        DEFAULT,
        ALLOW
    }

}
