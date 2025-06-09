package me.phoenixra.visor.core.client.gui;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.gui.VRCursorHandler;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.types.VROverlayScreen;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.VROverlayDraggedItem;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;


/**
 * (don't update variables here, they are like
 * a result of current state and all settings related to cursor summarized)
 */
public class VRCursorHandlerImpl implements VRCursorHandler {


    @Getter
    private ControllerHand cursorHand;
    @Getter
    private double cursorDisplayLength;

    @Getter
    private VROverlay focusedOverlay;


    @Getter
    private boolean bothCursorsDisplayed;

    @Getter
    private double cursorDisplayLength2;


    @Getter
    @Setter
    private boolean draggingItem;

    public VRCursorHandlerImpl() {

        ClientContext.cursorHandler = this;
    }

    public void process() {
        PoseData renderPose = ClientContext.player
                .getPose(PoseType.RENDER);


        CursorResult cursorResult = getCursorResult(
                null,
                renderPose
        );

        VROverlay collidedOverlay = cursorResult.collidedOverlay;
        Vec3 cursorPos = cursorResult.cursorPos;

        //finish
        if (collidedOverlay != null) {
            focusedOverlay = collidedOverlay;
            cursorHand = collidedOverlay.getCursorHand();
            cursorDisplayLength = cursorPos.z;
            bothCursorsDisplayed = collidedOverlay.isBothCursorsDisplayed();

            collidedOverlay.updateMousePosition(
                    true,
                    (float) cursorPos.x,
                    (float) cursorPos.y
            );

            if(bothCursorsDisplayed){
                CursorResult cursorResult2 = getCursorResult(
                        cursorHand.reversed(),
                        renderPose
                );
                if(cursorResult2.collidedOverlay == focusedOverlay){
                    cursorDisplayLength2 = cursorResult2.cursorPos.z;
                    collidedOverlay.updateMousePosition(
                            false,
                            (float) cursorResult2.cursorPos.x,
                            (float) cursorResult2.cursorPos.y
                    );
                }else{
                    bothCursorsDisplayed = false;
                }
            }
            if(!bothCursorsDisplayed
                    && collidedOverlay.getCursorSecondary().isInGui()){
                collidedOverlay.updateMousePosition(
                        false,
                        -1,
                        -1
                );
            }

        } else {
            focusedOverlay = null;
            cursorHand = null;
            cursorDisplayLength = -1;
            bothCursorsDisplayed = false;


            if (draggingItem) {
                VROverlayDraggedItem overlayDraggedItem =
                        (VROverlayDraggedItem) ClientContext.overlayManager
                                .getOverlay("dragged_item");
                PoseElement controllerPose = renderPose.getController(
                        overlayDraggedItem.getCursorHand()
                );
                cursorPos = getCursorCoordsInGuiWithDepth(
                        controllerPose,
                        overlayDraggedItem.getPosition(),
                        overlayDraggedItem.getRotation(),
                        overlayDraggedItem.getOverlayScale()
                );
                focusedOverlay = overlayDraggedItem;
                cursorHand = overlayDraggedItem.getCursorHand();
                cursorDisplayLength = cursorPos.z;
            }
        }


    }
    private CursorResult getCursorResult(@Nullable ControllerHand hand,
                                         @NotNull PoseData renderPose){
        VROverlay collidingOverlay = null;

        double lastDistanceZ = 1000;
        double screenDistanceZ = 1000;
        Vec3 cursor = new Vec3(0,0,-1);

        //check if cursor colliding with overlay
        for (VROverlay overlay : ClientContext.overlayManager
                .getOverlaysRegistry().getSortedElements()) {
            if (!overlay.isVisible()) continue;
            if (!overlay.isCursorSupported()) continue;
            PoseElement cursorElement = renderPose.getController(
                            hand == null
                                    ? overlay.getCursorHand()
                                    : hand
                    );

            double dist = getCursorDistanceToGui(
                    renderPose,
                    cursorElement,
                    overlay.getPosition(),
                    overlay.getRotation()
            );
            boolean mainCursor = hand == overlay.getCursorHand();
            if ((!overlay.ignoreFacingGui() && !isFacingGui(
                    overlay.getPosition(),
                    overlay.getRotation(),
                    cursorElement.getPosition(),
                    cursorElement.getRotationMatrix(),
                    false)
            ) || (dist > screenDistanceZ)) {
                overlay.updateMousePosition(
                        mainCursor,
                        -1,
                        -1
                );
                continue;
            }
            if(dist > lastDistanceZ){
                overlay.updateMousePosition(
                        mainCursor,
                        -1,
                        -1
                );
                continue;
            }

            Vec3 newCursor = getCursorCoordsInGuiWithDepth(
                    cursorElement,
                    overlay.getPosition(),
                    overlay.getRotation(),
                    overlay.getOverlayScale()
            );
            if (overlay.isCursorWithinBounds(
                    mainCursor,
                    (float) newCursor.x,
                    (float) newCursor.y
            )) {
                cursor = newCursor;
                collidingOverlay = overlay;
                lastDistanceZ = dist;
            } else {
                overlay.updateMousePosition(
                        mainCursor,
                        -1,
                        -1
                );
            }
        }
        return new CursorResult(cursor, collidingOverlay);
    }

    @Override
    public boolean isComponentAimedAtOverlay(@NotNull VROverlay overlay,
                                             @NotNull PoseElement component,
                                             boolean checkUpsideDown,
                                             float overlayBoundsExtraX,
                                             float overlayBoundsExtraY
    ) {

        if (!isFacingGui(
                overlay.getPosition(),
                overlay.getRotation(),
                component.getPosition(),
                component.getRotationMatrix(),
                checkUpsideDown
        )) {
            return false;
        }

        Vec3 newCursor = getCursorCoordsInGuiWithDepth(
                component,
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
    public @NotNull Vec2 getCursorCoordsInGui(@NotNull PoseElement component,
                                              @NotNull Vec3 guiPosRoom,
                                              @NotNull Matrix4fc guiRotationRoom,
                                              float guiScale
    ) {

        Vec3 vec3 = getCursorCoordsInGuiWithDepth(
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
    public Vec3 getCursorCoordsInGuiWithDepth(@NotNull PoseElement cursorElement,
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
                float guiHeight = ClientContext.guiManager.getScaledGuiHeight();
                float guiWidth = ClientContext.guiManager.getScaledGuiWidth();
                float aspectRatio = guiHeight / guiWidth;
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


    @Override
    public VROverlayScreen getFocusedOverlayAsScreen(){
        if(focusedOverlay instanceof VROverlayScreen overlayScreen){
            return overlayScreen;
        }
        return null;
    }
    @Override
    public boolean isCursorFocused() {
        return focusedOverlay != null;
    }

    private record CursorResult(
            Vec3 cursorPos,
            VROverlay collidedOverlay){

    }
}
