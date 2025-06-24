package me.phoenixra.visor.api.client.gui.overlay;


import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class VROverlayHelper {

    public static void renderImage(GuiGraphics guiGraphics,
                                   ResourceLocation resourceLocation,
                                   int posX, int posY,
                                   int width, int height,
                                   int textureWidth, int textureHeight) {
        guiGraphics.blit(
                resourceLocation,
                posX, posY,         // screen x, y
                0,                  // z-depth
                0.0F, 0.0F,         // texture u, v
                width, height,      // area to draw (width, height in pixels)
                textureWidth, textureHeight  // full texture size
        );
    }

    /**
     * Orient your overlay with a predefined OverlayOrient types
     * <br>
     * You can also adjust offsets for position and rotation
     *
     * @param overlay      The overlay to orient
     * @param positionType   The predefined position type
     * @param rotationType   The predefined rotation type
     * @param aimRotation    Should aim overlay at ModelViewLock rotation?
     * @param positionOffset The position offset
     * @param rotationOffset The rotation offset
     */
    public static void applyModelView(VROverlay overlay,
                                      ModelViewAnchor positionType,
                                      ModelViewAnchor rotationType,
                                      boolean aimRotation,
                                      Vector3f positionOffset,
                                      Vector3f rotationOffset
    ) {

        PoseData renderPose = VisorAPI.client().getPlayer()
                .getPose(PoseType.RENDER);
        overlay.setPosition(positionType.anchorPos(
                renderPose,
                positionOffset
        ));
        if(aimRotation){
            overlay.setRotation(rotationType.anchorRotationAim(
                    renderPose,
                    rotationOffset,
                    overlay.getPosition()
            ));
        }else {
            overlay.setRotation(rotationType.anchorRotation(
                    renderPose,
                    rotationOffset
            ));
        }
    }

    public static void anchorOverlayPositionTo(@NotNull VROverlay overlay,
                                               @NotNull PoseData renderPose,
                                               @NotNull Vector3fc objPosition,
                                               @NotNull Matrix4fc objRotation,
                                               @NotNull Vector3fc offset){
        float worldScale = renderPose.getWorldScale();
        offset = new Vector3f(
                offset.x() * worldScale,
                offset.y() * worldScale,
                offset.z() * worldScale
        );
        overlay.setPosition(
                getCustomVector(
                        offset,
                        objRotation
                ).add(objPosition)
        );
    }

    public static void anchorOverlayRotationTo(@NotNull VROverlay overlay,
                                               @NotNull Matrix4fc objRotation,
                                               @NotNull Vector3fc offset){
        Matrix4f overlayRot = objRotation.mul(
                new Matrix4f().rotationZ(offset.z()),
                new Matrix4f()
        );
        overlayRot.mul(new Matrix4f().rotationY(offset.y()));
        overlayRot.mul(new Matrix4f().rotationX(offset.x()));

        overlay.setRotation(overlayRot);
    }
    public static void anchorOverlayRotationToAimed(@NotNull VROverlay overlay,
                                                    @NotNull Vec3 objPosition,
                                                    @NotNull Vector3fc offset){

        var overlayPosition = overlay.getPosition();

        Vector3f directionToTarget = new Vector3f(
                (float) (overlayPosition.x() - objPosition.x),
                (float) (overlayPosition.y() - objPosition.y),
                (float) (overlayPosition.z() - objPosition.z)
        );
        float rotationX = (float) Math.asin(
                directionToTarget.y() / directionToTarget.length()
        );
        float rotationY = (float) (
                (double) (float) Math.PI +
                        Mth.atan2(
                                directionToTarget.x(),
                                directionToTarget.z()
                        )
        );
        Matrix4f rotation = new Matrix4f().rotationZ(offset.z());
        rotation.mul(new Matrix4f().rotationY(rotationY + offset.y()));
        rotation.mul(new Matrix4f().rotationX(rotationX + offset.x()));

        overlay.setRotation(rotation);
    }

    private static @NotNull Vector3f getCustomVector(@NotNull Vector3fc vec,
                                                     @NotNull Matrix4fc rotationMatrix) {
        return rotationMatrix
                .transformDirection(
                        new Vector3f(
                                vec.x(),
                                vec.y(),
                                vec.z()
                        )
                );
    }

}
