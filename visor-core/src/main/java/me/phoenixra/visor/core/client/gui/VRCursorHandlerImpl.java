package me.phoenixra.visor.core.client.gui;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.gui.VRCursorHandler;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;


public class VRCursorHandlerImpl implements VRCursorHandler {


    @Getter
    private ControllerHand activeCursorHand = ControllerHand.MAIN;

    @Getter
    @Setter
    private boolean draggingItem;

    @Getter
    private boolean isTwoHandedCursor;

    private final CursorState mainHandState = new CursorState();
    private final CursorState offhandState = new CursorState();

    public void process() {
        PoseData renderPose = ClientContext.player.getPose(PoseType.RENDER);

        updateCursorState(ControllerHand.MAIN, mainHandState, renderPose);
        updateCursorState(ControllerHand.OFFHAND, offhandState, renderPose);


        updateOverlays();
    }

    private void updateCursorState(@NotNull ControllerHand hand, @NotNull CursorState state, @NotNull PoseData renderPose) {
        VROverlay previouslyFocused = state.focusedOverlay;

        CursorResult result = getCursorResult(hand, renderPose);
        state.update(result.cursorPos, result.collidedOverlay);

        // Clean up previous focus
        if(previouslyFocused != null) {
            previouslyFocused.updateMousePosition(
                    true,
                    -1, -1
            );
            previouslyFocused.updateMousePosition(
                    false,
                    -1, -1
            );

        }
    }



    private void updateOverlays() {
        isTwoHandedCursor = offhandState.supportsTwoHandedCursor()
                || mainHandState.supportsTwoHandedCursor();

        CursorState activeState = (activeCursorHand == ControllerHand.MAIN) ? mainHandState : offhandState;
        CursorState inactiveState = (activeCursorHand == ControllerHand.MAIN) ? offhandState : mainHandState;

        // Update the overlay for the active hand
        if (activeState.isFocused()) {
            activeState.focusedOverlay.updateMousePosition(
                    true,
                    (float) activeState.cursorPos.x,
                    (float) activeState.cursorPos.y
            );
        }

        // Update the overlay for the inactive hand
        if (inactiveState.isFocused()) {
            inactiveState.focusedOverlay.updateMousePosition(
                    false,
                    (float) inactiveState.cursorPos.x,
                    (float) inactiveState.cursorPos.y
            );
        }

        // If the active hand is on an overlay that the inactive hand is not,
        // clear the inactive cursor position for that overlay
        if (activeState.isFocused()
                && activeState.focusedOverlay != inactiveState.focusedOverlay) {
            activeState.focusedOverlay.updateMousePosition(
                    false,
                    -1, -1
            );
        }
    }


    private CursorResult getCursorResult(@NotNull ControllerHand hand, @NotNull PoseData renderPose) {
        VROverlay collidingOverlay = null;
        Vec3 finalCursorPos = new Vec3(0, 0, -1);

        if(VisorState.getState().isNotFocused()){
            return new CursorResult(finalCursorPos, collidingOverlay);
        }

        double closestDistance = Double.MAX_VALUE;

        PoseElement cursorElement = renderPose.getController(hand);

        for (VROverlay overlay : ClientContext.overlayManager
                .getOverlaysRegistry().getSortedElements()) {
            if (!overlay.isVisible() || !overlay.isCursorSupported()) {
                continue;
            }

            double distance = getCursorDistanceToGui(
                    renderPose,
                    cursorElement,
                    overlay.getPosition(),
                    overlay.getRotation()
            );

            if (distance < 0 || distance > closestDistance) {
                continue;
            }

            boolean notFacingGui = !overlay.ignoreFacingGui()
                    && !isFacingGui(
                            overlay.getPosition(),
                            overlay.getRotation(),
                            cursorElement.getPosition(),
                            cursorElement.getRotationMatrix(),
                    false
            );
            if (notFacingGui) {
                continue;
            }

            Vec3 newCursorPos = findCursorGuiCoordinates3D(
                    cursorElement,
                    overlay.getPosition(),
                    overlay.getRotation(),
                    overlay.getOverlayScale()
            );

            if (overlay.isCursorWithinBounds(
                    true,
                    (float) newCursorPos.x,
                    (float) newCursorPos.y
            )) {
                finalCursorPos = newCursorPos;
                collidingOverlay = overlay;
                closestDistance = distance;
            }
        }
        return new CursorResult(finalCursorPos, collidingOverlay);
    }

    @Override
    public void changeActiveCursorHand(@NotNull ControllerHand hand) {
        this.activeCursorHand = hand;
    }

    @Override
    public double getCursorLength(@NotNull ControllerHand hand) {
        return (hand == ControllerHand.MAIN) ? mainHandState.getCursorLength() : offhandState.getCursorLength();
    }

    @Override
    public @Nullable VROverlay getFocusedOverlay(@NotNull ControllerHand hand) {
        return (hand == ControllerHand.MAIN) ? mainHandState.focusedOverlay : offhandState.focusedOverlay;
    }


    @Override
    public boolean isElementAimedAtOverlay(@NotNull VROverlay overlay,
                                           @NotNull PoseElement element,
                                           boolean checkUpsideDown,
                                           float overlayBoundsExtraX,
                                           float overlayBoundsExtraY
    ) {

        if (!isFacingGui(
                overlay.getPosition(),
                overlay.getRotation(),
                element.getPosition(),
                element.getRotationMatrix(),
                checkUpsideDown
        )) {
            return false;
        }

        Vec3 newCursor = findCursorGuiCoordinates3D(
                element,
                overlay.getPosition(),
                overlay.getRotation(),
                overlay.getOverlayScale()
        );
        if (overlayBoundsExtraX != 0 || overlayBoundsExtraY != 0) {
            float multX = overlayBoundsExtraX / 2;
            float multY = overlayBoundsExtraY / 2;
            double x;
            double y;
            if ((newCursor.x < 0.5 && newCursor.x >= -multX)
                    || (newCursor.x > 0.5 && newCursor.x <= 1 + multX)) {
                x = 0.5;
            } else {
                x = newCursor.x;
            }
            if ((newCursor.y < 0.5 && newCursor.y >= -multY)
                    || (newCursor.y > 0.5 && newCursor.y <= 1 + multY)) {
                y = 0.5;
            } else {
                y = newCursor.y;
            }
            newCursor = new Vec3(x, y, 0);
        }


        return overlay.isCursorWithinBounds(
                true,
                (float) newCursor.x,
                (float) newCursor.y
        );
    }


    public boolean isFacingGui(Vec3 guiPosition,
                               Matrix4fc guiRotation,
                               Vec3 devicePosition,
                               Matrix4fc deviceRotation,
                               boolean checkUpsideDown
    ) {
        return isFacingGui(
                guiPosition,
                guiRotation,
                devicePosition,
                deviceRotation,
                checkUpsideDown,0.2
        );
    }
    public boolean isFacingGui(Vec3 guiPosition,
                               Matrix4fc guiRotation,
                               Vec3 devicePosition,
                               Matrix4fc deviceRotation,
                               boolean checkUpsideDown,
                               double threshold
    ) {


        Vector3f deviceForward = extractForwardDirection(deviceRotation);
        Vector3f guiForward = extractForwardDirection(guiRotation);

        Vector3f deviceToGui = new Vector3f(guiPosition.toVector3f())
                .sub(devicePosition.toVector3f());

        deviceForward.normalize();
        guiForward.normalize();
        deviceToGui.normalize();

        float deviceFacingGui = deviceForward.dot(deviceToGui);
        float GuiFacingDevice = guiForward.dot(deviceToGui.negate());


        boolean isFacingGui = deviceFacingGui > threshold && GuiFacingDevice < -threshold;

        if (!isFacingGui) {
            return false;
        }

        if (!checkUpsideDown) return true;


        Vector3f deviceUp = extractUpDirection(deviceRotation);
        Vector3f guiUp = extractUpDirection(guiRotation);
        deviceUp.normalize();
        guiUp.normalize();

        float guiUpAlignment = guiUp.dot(
                new Vector3f(0, 1, 0)
        );

        return guiUpAlignment > 0.2;
    }

    private Vector3f extractUpDirection(Matrix4fc matrix) {
        return new Vector3f(matrix.m10(), matrix.m11(), matrix.m12());
    }

    private Vector3f extractForwardDirection(Matrix4fc rotation) {
        return new Vector3f(-rotation.m20(), -rotation.m21(), -rotation.m22());
    }

    @Override
    public @NotNull Vec2 findCursorGuiCoordinates2D(@NotNull PoseElement component,
                                                    @NotNull Vec3 guiPosRoom,
                                                    @NotNull Matrix4fc guiRotationRoom,
                                                    float guiScale
    ) {

        Vec3 vec3 = findCursorGuiCoordinates3D(
                component,
                guiPosRoom, guiRotationRoom,
                guiScale
        );
        return new Vec2((float) vec3.x, (float) vec3.y);
    }

    public float getCursorDistanceToGui(@NotNull PoseData clientPose,
                                        @NotNull PoseElement cursorElement,
                                        @NotNull Vec3 guiPosRoom,
                                        @NotNull Matrix4fc guiRotationRoom){
        Vector3fc position = cursorElement.getPosition().toVector3f();
        Vector3fc direction = cursorElement.getDirection().toVector3f();
        Vector3fc aimZ = guiRotationRoom.transformDirection(VRMathUtils.forwardVectorReversed, new Vector3f());
        Vector3fc aimX = guiRotationRoom.transformDirection(VRMathUtils.rightVector, new Vector3f());
        Vector3fc aimY = guiRotationRoom.transformDirection(VRMathUtils.upVector, new Vector3f());
        float point = aimZ.dot(direction);

        if (Math.abs(point) > 1.0E-5F) {
            Vector3f cursorBase = guiPosRoom.toVector3f()
                    .sub(
                            aimY.div(2f, new Vector3f())
                    ).sub(
                            aimX.div(2f, new Vector3f())
                    );
            float depthFactor = -aimZ.dot(
                    position.sub(cursorBase, new Vector3f())
            ) / point;

            return depthFactor / clientPose.getWorldScale();
        }
        return -1;
    }
    @Override
    public Vec3 findCursorGuiCoordinates3D(@NotNull PoseElement cursorElement,
                                           @NotNull Vec3 guiPosRoom,
                                           @NotNull Matrix4fc guiRotationRoom,
                                           float guiScale
    ) {
        PoseData renderPose = ClientContext.player
                .getPose(PoseType.RENDER);
        guiScale *= renderPose.getWorldScale();
        Vector3fc position = cursorElement.getPosition().toVector3f();
        Vector3fc direction = cursorElement.getDirection().toVector3f();
        Vector3fc aimZ = guiRotationRoom.transformDirection(VRMathUtils.forwardVectorReversed, new Vector3f());
        Vector3fc aimX = guiRotationRoom.transformDirection(VRMathUtils.rightVector, new Vector3f());
        Vector3fc aimY = guiRotationRoom.transformDirection(VRMathUtils.upVector, new Vector3f());
        float point = aimZ.dot(direction);

        if (Math.abs(point) > 1.0E-5F) {
            Vector3f cursorBase = guiPosRoom.toVector3f()
                    .sub(
                            aimY.div(2f, new Vector3f())
                    ).sub(
                            aimX.div(2f, new Vector3f())
                    );
            float depthFactor = -aimZ.dot(
                    position.sub(cursorBase, new Vector3f())
            ) / point;

            if (depthFactor > 0.0F) {
                Vector3fc cursorPosition3D = position.add(direction.div(1.0F / depthFactor, new Vector3f()), new Vector3f());
                Vector3fc cursorOffset = cursorPosition3D.sub(cursorBase, new Vector3f());
                float cursorX = cursorOffset.dot(aimX);
                float cursorY = cursorOffset.dot(aimY);
                float aspectRatio = ClientContext.guiManager.getScaledAspectRatio();
                cursorX = (cursorX - 0.5F) / 1.5F / guiScale + 0.5F;
                cursorY = (cursorY - 0.5F) / aspectRatio / 1.5F / guiScale + 0.5F;
                cursorY = 1.0F - cursorY;
                return new Vec3(
                        cursorX,
                        cursorY,
                        depthFactor
                                / renderPose.getWorldScale());
            }
        }

        return new Vec3(-1.0F, -1.0F, -1.0f);
    }



    private record CursorResult(Vec3 cursorPos, VROverlay collidedOverlay) {
    }

    private static class CursorState {
        private VROverlay focusedOverlay;
        private Vec3 cursorPos = new Vec3(-1, -1, -1);

        void update(@NotNull Vec3 newCursorPos, @Nullable VROverlay newFocusedOverlay) {
            this.cursorPos = newCursorPos;
            this.focusedOverlay = newFocusedOverlay;
        }

        boolean isFocused() {
            return focusedOverlay != null;
        }
        boolean supportsTwoHandedCursor(){
            return focusedOverlay != null && focusedOverlay.supportsTwoHandedCursor();
        }

        double getCursorLength() {
            return isFocused() ? cursorPos.z : -1;
        }
    }
}
