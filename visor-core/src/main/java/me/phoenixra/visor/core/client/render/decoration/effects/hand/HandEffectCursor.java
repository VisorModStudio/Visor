package me.phoenixra.visor.core.client.render.decoration.effects.hand;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRHandEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.hand.HandRenderStage;
import me.phoenixra.visor.api.client.render.decoration.effects.hand.VRHandEffectBase;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.compatibility.ShadersHelper;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.VRCursorHandlerImpl;
import me.phoenixra.visor.core.client.render.helpers.RenderHelper;
import me.phoenixra.visor.core.client.render.helpers.TexturesHelper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


@RegisterVRHandEffect
public class HandEffectCursor extends VRHandEffectBase {
    private static final String ID = "cursor";

    public HandEffectCursor(@NotNull VisorAddon owner){
        super(owner);
    }
    @Override
    public void render(@NotNull ControllerHand hand,
                       @NotNull VRDisplay renderDisplay,
                       @NotNull PoseStack poseStack,
                       boolean simpleHand,
                       float partialTicks) {

        VRCursorHandlerImpl cursorHandler = ClientContext.cursorHandler;

        double cursorLength = cursorHandler.getCursorLength(hand);
        if(cursorLength <= 0){
            return;
        }

        RenderSystem.disableDepthTest();

        if (MC.getOverlay() == null) {
            MC.getTextureManager().bindForSetup(TexturesHelper.getWhiteTexture());
            RenderSystem.setShaderTexture(0, TexturesHelper.getWhiteTexture());
        }

        Tesselator tesselator = Tesselator.getInstance();


        Vec3i color = new Vec3i(228, 228, 228);
        byte alpha = (byte) 255;


        Vec3 start = new Vec3(0.0D, 0.0D, 0.0D);
        Vec3 end = new Vec3(
                start.x,
                start.y,
                start.z - cursorLength
        );

        if (MC.level != null) {
            float light = (float) MC.level.getMaxLocalRawBrightness(
                    BlockPos.containing(
                            ClientContext.player
                                    .getPose(PoseType.RENDER)
                                    .getHmd()
                                    .getPosition()
                    )
            );

            int minLight = ShadersHelper.shaderLight();

            if (light < (float) minLight) {
                light = (float) minLight;
            }

            float lightPercent = light / (float) MC.level.getMaxLightLevel();
            color = new Vec3i(Mth.floor(color.getX() * lightPercent), Mth.floor(color.getY() * lightPercent),
                    Mth.floor(color.getZ() * lightPercent));
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        tesselator.getBuilder().begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR_NORMAL
        );
        RenderHelper.renderBox(tesselator.getBuilder(),
                start, end,
                -0.0016f, 0.0016f,
                -0.0016f, 0.0016f,
                color,
                alpha,
                poseStack
        );
        BufferUploader.drawWithShader(tesselator.getBuilder().end());

    }

    @Override
    public boolean isVisible(ControllerHand hand, boolean simpleHand) {
        VRCursorHandlerImpl cursorHandler = ClientContext.cursorHandler;

        if(cursorHandler.isTwoHandedCursor()){
            return true;
        }

        return cursorHandler.getActiveCursorHand() == hand;
    }


    @Override
    public HandRenderStage renderAtStage() {
        return HandRenderStage.BEFORE_RENDERED;
    }


    @Override
    public @NotNull String getId() {
        return ID;
    }
}
