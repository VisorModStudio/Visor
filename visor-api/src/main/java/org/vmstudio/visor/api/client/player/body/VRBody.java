package org.vmstudio.visor.api.client.player.body;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.render.decoration.VRBodyRenderer;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.VisorComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class VRBody implements VisorComponent {

    @Getter
    @NotNull
    private final VisorAddon owner;
    @Getter @Setter
    private boolean enabled = true;


    private final Map<String, VRBodyPart> bodyPartsMap;

    public VRBody(@NotNull VisorAddon owner){
        Objects.requireNonNull(owner);
        this.owner = owner;
        bodyPartsMap = new HashMap<>();
    }

    public abstract void onInit();

    @NotNull
    public abstract VRBodyPart getMainHand();
    @NotNull
    public abstract VRBodyPart getOffhand();


    @NotNull
    public abstract VRBodyRenderer getRenderer();

    /**
     * If player can select this body.
     * When false, only selectable
     *
     * @return true/false
     */
    public abstract boolean isSelectable();

    /**
     * If First person body is used
     *
     * @return true/false
     */
    public abstract boolean isFullBody();


    public abstract Component getName();

    public final void init(){
        clearBody();
        onInit();
    }


    protected void addBodyPart(@NotNull VRBodyPart bodyPart){
        bodyPartsMap.put(bodyPart.getId(), bodyPart);
    }
    protected void clearBody(){
        bodyPartsMap.clear();
    }


    @NotNull
    public VRBodyPart getBodyPart(@NotNull String id){
        return bodyPartsMap.get(id);
    }


    public Collection<VRBodyPart> getAllBodyParts(){
        return bodyPartsMap.values();
    }



}
