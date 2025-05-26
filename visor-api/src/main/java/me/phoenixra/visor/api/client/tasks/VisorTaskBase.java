package me.phoenixra.visor.api.client.tasks;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


@Getter
public abstract class VisorTaskBase implements VisorTask {
    @NotNull
    private final VisorAddon owner;
    @Setter
    private boolean enabled = true;

    private boolean cleared;


    public VisorTaskBase(@NotNull VisorAddon owner){
        Objects.requireNonNull(owner);
        this.owner = owner;
    }

    protected abstract void onRun(@Nullable LocalPlayer player);

    protected abstract void onClear(@Nullable LocalPlayer player);


    @Override
    public final void run(@Nullable LocalPlayer player) {
        cleared = false;
        onRun(player);
    }

    @Override
    public final void clear(@Nullable LocalPlayer player) {
        if(cleared) return;
        onClear(player);
        cleared = true;
    }

    @Override
    public int compareTo(@NotNull VisorTask o) {
        return getPriority() - o.getPriority();
    }

}
