package me.phoenixra.visor.api.client.tasks;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.element.PrioritySupporter;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.element.VisorElement;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter
public abstract class VisorTask implements VisorElement, PrioritySupporter {
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

    public abstract boolean isActive(@Nullable LocalPlayer player);


    public final void run(@Nullable LocalPlayer player) {
        cleared = false;
        onRun(player);
    }

    public final void clear(@Nullable LocalPlayer player) {
        if(!alwaysClear() && cleared) return;
        onClear(player);
        cleared = true;
    }


    public boolean isEnabledAndActive(@Nullable LocalPlayer player) {
        return enabled && isActive(player);
    }



    protected boolean alwaysClear(){
        return false;
    }

    @Override
    public @NotNull ElementPriority getPriority() {
        return ElementPriority.NORMAL;
    }

    @NotNull
    public TaskType getType(){
        return TaskType.VR_PRE_TICK;
    }
}
