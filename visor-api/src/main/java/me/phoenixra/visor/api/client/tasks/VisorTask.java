package me.phoenixra.visor.api.client.tasks;

import me.phoenixra.visor.api.common.addon.VisorElement;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface VisorTask extends VisorElement, Comparable<VisorTask> {



    void run(@Nullable LocalPlayer player);

    /**
     * Clear up cache.
     */
    default void clear(@Nullable LocalPlayer player){
    }

    /**
     * If task is active
     * @param player use null if not required
     */
   default boolean isActive(@Nullable LocalPlayer player){
       return isEnabled();
   }


    /**
     * Get priority level of the task.
     * <p>
     * The priority determines the order in which
     * the task is processed during ticking.
     * A higher priority value means the task
     * will be called earlier.
     * </p>
     *
     * @return priority
     */
    default int getPriority(){
        return 100;
    }


    @NotNull
    default TaskType getType(){
        return TaskType.VR_PRE_TICK;
    }
}
