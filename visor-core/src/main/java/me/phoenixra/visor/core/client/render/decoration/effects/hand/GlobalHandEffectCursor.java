package me.phoenixra.visor.core.client.render.decoration.effects.hand;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.misc.color.AtumColorImmutable;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.data.PoseDataType;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


/**
 * Hand effect with
 * {@link #isGlobal()}= true
 */
@RegisterVRHandEffect()
public class GlobalHandEffectCursor extends VRHandEffect {
    public static final String ID = "cursor";

    private static final AtumColorImmutable DEFAULT_COLOR = new AtumColorImmutable(
            228, 228, 228,
            255
    );

    private static final float BOX_HALF_SIZE = 0.0016f;

    public GlobalHandEffectCursor(@NotNull VisorAddon owner){
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
        float cursorLength = (float) cursorHandler.getCursorLineLength(hand);
        if (cursorLength <= 0) {
            return;
        }

        Vector3f start = new Vector3f(0, 0, 0);
        Vector3f end = new Vector3f(0, 0, -cursorLength);

        // compute brightness-tinted color
        AtumColorImmutable color;
        if (MC.level != null) {
            float rawLight = MC.level.getMaxLocalRawBrightness(
                    BlockPos.containing(
                            new Vec3(
                                    (Vector3f) ClientContext.player
                                    .getPoseData(PoseDataType.RENDER)
                                    .getHmd()
                                    .getPosition()
                            )
                    )
            );
            float minLight = ShadersHelper.shaderLight();
            float light = Math.max(rawLight, minLight);
            float lightPercent = light / MC.level.getMaxLightLevel();
            color = new AtumColorImmutable(
                    Mth.floor(DEFAULT_COLOR.getRedInt() * lightPercent),
                    Mth.floor(DEFAULT_COLOR.getGreenInt() * lightPercent),
                    Mth.floor(DEFAULT_COLOR.getBlueInt() * lightPercent),
                    255
            );
        }else{
            color = DEFAULT_COLOR;
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

        RenderHelper.renderCuboid(
                builder,
                poseStack.last().pose(),
                start, end,
                -BOX_HALF_SIZE, BOX_HALF_SIZE,
                -BOX_HALF_SIZE, BOX_HALF_SIZE,
                color
        );


        // --- Restore GL ---
        RenderSystem.enableDepthTest();

    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator,
                             @NotNull ControllerHand hand,
                             boolean simpleHand) {
        VRCursorHandlerImpl cursorHandler = ClientContext.cursorHandler;

        if(!ClientContext.visor.isFeatureEnabled(ClientFeature.GUI_CURSOR)){
            return false;
        }

        if(cursorHandler.isTwoHandedCursor()){
            return true;
        }
        if(!cursorHandler.isCursorHandFocused()){
            return false;
        }


        return cursorHandler.getCursorHand() == hand;
    }


    @Override
    public boolean isGlobal() {
        return true;
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
