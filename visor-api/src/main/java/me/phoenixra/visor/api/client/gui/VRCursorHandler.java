package me.phoenixra.visor.api.client.gui;

import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.api.common.ControllerHand;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;


public interface VRCursorHandler {


    @NotNull
    ControllerHand getActiveCursorHand();
    void setActiveCursorHand(@NotNull ControllerHand hand);


    double getCursorLength(@NotNull ControllerHand hand);


    @Nullable
    VROverlay getFocusedOverlay(@NotNull ControllerHand hand);

    default VROverlay getFocusedOverlay(){
        return getFocusedOverlay(getActiveCursorHand());
    }


    @Nullable
    default VROverlayScreen getFocusedOverlayAsScreen(@NotNull ControllerHand hand){
        if(getFocusedOverlay(hand) instanceof VROverlayScreen overlayScreen){
            return overlayScreen;
        }
        return null;
    }
    default VROverlayScreen getFocusedOverlayAsScreen(){
        return getFocusedOverlayAsScreen(getActiveCursorHand());
    }



    default boolean isAnyHandFocused(){
        return isMainHandFocused() || isOffhandFocused();
    }

    default boolean isActiveHandFocused(){
        return getFocusedOverlay(getActiveCursorHand()) != null;
    }
    default boolean isHandFocused(@NotNull ControllerHand hand){
        return getFocusedOverlay(hand) != null;
    }
    default boolean isMainHandFocused(){
        return getFocusedOverlay(ControllerHand.MAIN) != null;
    }
    default boolean isOffhandFocused(){
        return getFocusedOverlay(ControllerHand.OFFHAND) != null;
    }

    boolean isTwoHandedCursor();


    boolean isDraggingItem();


    @ApiStatus.Internal
    void setDraggingItem(boolean flag);



    boolean isElementAimedAtOverlay(@NotNull VROverlay overlay,
                                    @NotNull PoseElement element,
                                    boolean checkUpsideDown,
                                    float overlayBoundsExtraX,
                                    float overlayBoundsExtraY);



    @NotNull
    Vec2 findCursorGuiCoordinates2D(@NotNull PoseElement component,
                                    @NotNull Vec3 guiPosRoom,
                                    @NotNull Matrix4fc guiRotationRoom,
                                    float guiScale);



    Vec3 findCursorGuiCoordinates3D(@NotNull PoseElement component,
                                    @NotNull Vec3 guiPosRoom,
                                    @NotNull Matrix4fc guiRotationRoom,
                                    float guiScale);


}
