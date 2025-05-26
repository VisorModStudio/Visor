package me.phoenixra.visor.api.client.tasks;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.VisorElement;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


@Getter
public abstract class VisorTask implements VisorElement, Comparable<VisorTask>  {
    @NotNull
    private final VisorAddon owner;
    @Setter
    private boolean enabled = true;

    private boolean cleared;


    public VisorTask(@NotNull VisorAddon owner){
        Objects.requireNonNull(owner);
        this.owner = owner;
    }

    protected abstract void onRun(@Nullable LocalPlayer player);

    protected abstract void onClear(@Nullable LocalPlayer player);



    public final void run(@Nullable LocalPlayer player) {
        cleared = false;
        onRun(player);
    }

    public final void clear(@Nullable LocalPlayer player) {
        if(cleared) return;
        onClear(player);
        cleared = true;
    }

    @Override
    public int compareTo(@NotNull VisorTask o) {
        return getPriority() - o.getPriority();
    }


    /**
     * If task is active
     * @param player use null if not required
     */
    public boolean isActive(@Nullable LocalPlayer player){
        return isEnabled();
    }


    /**
     * Get priority level of the task.
     * <p>
     * The priority determines the order in which
     * the task is processed.
     * A higher priority value means the task
     * will be called earlier.
     * </p>
     *
     * @return priority
     */
    public int getPriority(){
        return 100;
    }


    @NotNull
    public TaskType getType(){
        return TaskType.VR_PRE_TICK;
    }
}
