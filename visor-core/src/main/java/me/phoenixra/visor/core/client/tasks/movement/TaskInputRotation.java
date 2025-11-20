package me.phoenixra.visor.core.client.tasks.movement;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskInputRotation extends VisorTask {
    private static final String ID = "input_rotation";

    @Getter
    private static TaskInputRotation instance;

    @Getter @Setter
    private float inputRotation = 0;

    public TaskInputRotation(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    protected void onRun(LocalPlayer player) {
        if(inputRotation == 0){
            return;
        }
        ClientContext.player.setRotationY(ClientContext.player.getRotationY()+inputRotation);
        inputRotation = 0;
    }

    @Override
    protected void onClear(@Nullable LocalPlayer player) {
        inputRotation = 0;
    }

    @Override
    public boolean isActive(LocalPlayer p) {
        return MC.level != null;
    }

    @Override
    public @NotNull TaskType getType() {
        return TaskType.VR_PLAYER_TICK;
    }

    @Override
    public @NotNull ElementPriority getPriority() {
        return ElementPriority.HIGH;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
