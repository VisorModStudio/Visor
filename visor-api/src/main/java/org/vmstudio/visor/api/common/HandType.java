package org.vmstudio.visor.api.common;

import me.phoenixra.atumvr.api.enums.ControllerType;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.common.player.VRTrackableBodyPart;
import net.minecraft.world.InteractionHand;

public enum HandType {
    MAIN,
    OFFHAND;


    public @NotNull VRTrackableBodyPart asBodyPart(){
        return this == MAIN ? VRTrackableBodyPart.MAIN_HAND : VRTrackableBodyPart.OFFHAND;
    }

    public @NotNull InteractionHand asInteractionHand(){
        return this == MAIN ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public @NotNull HumanoidArm asHumanoidArm(boolean leftHanded){
        if(leftHanded){
            return this == MAIN ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        }else{
            return this == MAIN ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        }
    }
    public @NotNull ControllerType asControllerType(boolean leftHanded){
        if(leftHanded){
            return this == MAIN ? ControllerType.LEFT : ControllerType.RIGHT;
        }else{
            return this == MAIN ? ControllerType.RIGHT : ControllerType.LEFT;
        }
    }

    public @NotNull HandType opposite(){
        if(this == OFFHAND) return MAIN;
        else return OFFHAND;
    }

    public static @NotNull HandType fromInt(int id){
        if(id == 0) return MAIN;
        return OFFHAND;
    }

    public static @NotNull HandType fromMc(InteractionHand mcHand){
        return mcHand == InteractionHand.MAIN_HAND ? MAIN : OFFHAND;
    }
    public static @NotNull HandType fromMcArm(HumanoidArm mcArm,
                                              boolean leftHanded){
        if(leftHanded){
            return mcArm == HumanoidArm.LEFT ? MAIN : OFFHAND;
        }else{
            return mcArm == HumanoidArm.RIGHT ? MAIN : OFFHAND;
        }
    }

    public static @NotNull HandType fromController(@NotNull ControllerType controllerType,
                                                   boolean leftHanded){
        if(leftHanded){
            return controllerType == ControllerType.LEFT ? MAIN : OFFHAND;
        }else{
            return controllerType == ControllerType.LEFT ? OFFHAND : MAIN;
        }
    }
}
