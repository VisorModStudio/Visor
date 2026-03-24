package org.vmstudio.visor.api.client.player.body;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.render.decoration.VRBodyRenderer;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.VisorComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VRBody {

    @Getter
    private final VRBodyType type;

    @Getter
    private final VRClientPlayer vrPlayer;


    private final Map<String, VRBodyPart> bodyPartsMap;


    @Getter
    private VRBodyPart head;
    @Getter
    private VRBodyPart mainHand;
    @Getter
    private VRBodyPart offhand;

    public VRBody(@NotNull VRBodyType type,
                  @NotNull VRClientPlayer vrPlayer){
        this.type = type;
        this.vrPlayer = vrPlayer;
        bodyPartsMap = new HashMap<>();
    }

    public void onInit(){
        //override
    }


    public final void init(){
        clearBody();

        head = VRBodyPart.SIMPLE_HEAD;
        mainHand = VRBodyPart.SIMPLE_MAIN_HAND;
        offhand = VRBodyPart.SIMPLE_OFFHAND;
        addBodyPart(mainHand);
        addBodyPart(offhand);

        onInit();
    }

    public float getBodyYaw(){
        return head.pose.getYaw();
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
