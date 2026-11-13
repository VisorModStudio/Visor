package org.vmstudio.visor.api.client.input.action.framework;

import lombok.Getter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.input.action.VRActionSet;

public class VRActionKeyCustom extends VRActionKey {

    private final Component name;
    @Getter
    private final String langKeyName;

    public VRActionKeyCustom(@NotNull String id,
                             @NotNull VRActionSet actionSet,
                             char character,
                             @NotNull String langKeyName) {
        super(id, actionSet, character);
        this.name = Component.translatable(langKeyName);
        this.langKeyName = langKeyName;
    }


    @Override
    public @NotNull Component getName() {
        return name;
    }


}
