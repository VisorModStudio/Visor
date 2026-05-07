package org.vmstudio.visor.api.client.player.body;

import lombok.Getter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.player.VRPose;

import java.util.*;

//TODO make it both sided? for later cool usage
// (server will calculate positioning, no extra network usage will be needed)
public class VRBody {

    @Getter
    protected final VRBodyType type;

    @Getter
    protected final VRClientPlayer vrPlayer;

    @Getter
    protected final VRPlayerPoseClient vrPlayerPose;


    protected Map<String, VRBodyPart> bodyPartsMap;

    @Getter
    protected List<VRPose> allPoses;

    @Getter
    protected VRBodyPart head;
    @Getter
    protected VRBodyPart mainHand;
    @Getter
    protected VRBodyPart offhand;

    public VRBody(@NotNull VRBodyType type,
                  @NotNull VRClientPlayer vrPlayer,
                  @NotNull VRPlayerPoseClient vrPlayerPose){
        this.type = type;
        this.vrPlayer = vrPlayer;
        this.vrPlayerPose = vrPlayerPose;
        this.bodyPartsMap = new HashMap<>();
        this.allPoses = new ArrayList<>();
    }

    public void onInit(){
        //override
    }


    public final void init(){
        clear();

        head = VRBodyPart.createSimpleHead();
        mainHand = VRBodyPart.createSimpleMainHand();
        offhand = VRBodyPart.createSimpleOffhand();
        addBodyPart(head);
        addBodyPart(mainHand);
        addBodyPart(offhand);

        onInit();
    }

    public void update(){
        bodyPartsMap.values().forEach(it->it.update(vrPlayerPose));
    }

    public void copyFrom(@NotNull VRBody other){
        if(other.getType() != type){
            throw new VRException(
                    Component.literal("Failed VRBody copying"),
                    Component.literal("Tried to copyFrom VRBody that has different type")
            );
        }
        for(var bodyPart : bodyPartsMap.values()){
            var otherBodyPart = other.getBodyPart(bodyPart.getId());
            if(otherBodyPart == null) continue;
            bodyPart.copyFrom(otherBodyPart);
        }
    }
    protected void clear(){
        bodyPartsMap.clear();
        allPoses.clear();
    }

    public float getBodyYaw(){
        return head.pose.getYaw();
    }

    protected void addBodyPart(@NotNull VRBodyPart bodyPart){
        bodyPartsMap.put(bodyPart.getId(), bodyPart);
        allPoses.add(bodyPart.pose);
    }


    @Nullable
    public VRBodyPart getBodyPart(@NotNull String id){
        return bodyPartsMap.get(id);
    }


    public Collection<VRBodyPart> getAllBodyParts(){
        return bodyPartsMap.values();
    }


    public VRBodyPart getHand(@NotNull HandType handType){
        return handType == HandType.MAIN ? mainHand : offhand;
    }

}
