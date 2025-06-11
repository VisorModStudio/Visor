package me.phoenixra.visor.core.client.render.decoration.effects.hand;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRHandEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.VRHandEffect;
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
public class HandEffectCursor extends VRHandEffect {
    private static final String ID = "cursor";

    private static final Vec3i DEFAULT_COLOR = new Vec3i(228, 228, 228);
    private static final byte DEFAULT_ALPHA = (byte) 255;
    private static final float BOX_HALF_SIZE = 0.0016f;

    public HandEffectCursor(@NotNull VisorAddon owner){
        super(owner);
    }
    @Override
    public void render(@NotNull ControllerHand hand,
                       @NotNull VRDisplay renderDisplay,
                       @NotNull PoseStack poseStack,
                       boolean simpleHand,
                       float partialTicks) {

        // --- Prepare variables ---
        VRCursorHandlerImpl cursorHandler = ClientContext.cursorHandler;
        double cursorLength = cursorHandler.getCursorLength(hand);
        if (cursorLength <= 0) {
            return;
        }

        Vec3 start = new Vec3(0, 0, 0);
        Vec3 end = new Vec3(0, 0, -cursorLength);

        // compute brightness-tinted color
        Vec3i color = DEFAULT_COLOR;
        if (MC.level != null) {
            float rawLight = MC.level.getMaxLocalRawBrightness(
                    BlockPos.containing(
                            ClientContext.player
                                    .getPose(PoseType.RENDER)
                                    .getHmd()
                                    .getPosition()
                    )
            );
            float minLight = ShadersHelper.shaderLight();
            float light = Math.max(rawLight, minLight);
            float pct = light / MC.level.getMaxLightLevel();
            color = new Vec3i(
                    Mth.floor(DEFAULT_COLOR.getX() * pct),
                    Mth.floor(DEFAULT_COLOR.getY() * pct),
                    Mth.floor(DEFAULT_COLOR.getZ() * pct)
            );
        }

        BufferBuilder builder = Tesselator.getInstance().getBuilder();

        // --- GL setup ---
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        if (MC.getOverlay() == null) {
            var whiteTex = TexturesHelper.getWhiteTexture();
            MC.getTextureManager().bindForSetup(whiteTex);
            RenderSystem.setShaderTexture(0, whiteTex);
        }


        // --- Render ---
        builder.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR_NORMAL
        );
        RenderHelper.renderBox(
                builder,
                start, end,
                -BOX_HALF_SIZE, BOX_HALF_SIZE,
                -BOX_HALF_SIZE, BOX_HALF_SIZE,
                color,
                DEFAULT_ALPHA,
                poseStack
        );
        BufferUploader.drawWithShader(builder.end());

        // --- Restore GL ---
        RenderSystem.enableDepthTest();

    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator,
                             @NotNull ControllerHand hand,
                             boolean simpleHand) {
        VRCursorHandlerImpl cursorHandler = ClientContext.cursorHandler;

        if(cursorHandler.isTwoHandedCursor()){
            return true;
        }
        if(!cursorHandler.isActiveHandFocused()){
            return false;
        }

        return cursorHandler.getActiveCursorHand() == hand;
    }


    @Override
    public RenderStage renderAtStage() {
        return RenderStage.BEFORE_HANDS;
    }


    @Override
    public @NotNull String getId() {
        return ID;
    }
}
