package org.vmstudio.visor.api.client.input.action.framework;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class VRActionKey extends VRActionButton {


    @Getter
    private final char character;

    @Getter
    private final String mcKey;

    // KEY
    public VRActionKey(@NotNull String id,
                       @NotNull VRActionSet actionSet,
                       char character) {
        super(actionSet, id);
        this.character = character;
        this.mcKey = null;
    }

    // MC KEY
    public VRActionKey(@NotNull String id,
                       @NotNull VRActionSet actionSet,
                       @NotNull String mcKey) {
        super(actionSet, id);
        this.character = (char) -1;
        this.mcKey = mcKey;
    }


    @Override
    protected void onPress() {
        if(isUsingMcKey()) InputHelper.updateKeyMapping(this.mcKey, true);
        else InputHelper.pressChar(character);
    }

    @Override
    protected void onRelease() {
        if(isUsingMcKey()) InputHelper.updateKeyMapping(this.mcKey, false);
        else InputHelper.releaseChar(character);
    }


    public boolean isUsingMcKey(){
        return mcKey != null;
    }

    @Override
    public @NotNull Component getName() {
        return isUsingMcKey() ? Component.translatable(mcKey) : super.getName();
    }

    // Optional, to allow creating object without implementation
    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        //empty
        return Map.of();
    }
}
