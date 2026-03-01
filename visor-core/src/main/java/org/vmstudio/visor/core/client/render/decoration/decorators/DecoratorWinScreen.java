package org.vmstudio.visor.core.client.render.decoration.decorators;

import com.mojang.blaze3d.vertex.PoseStack;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRDecorator;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.helpers.VREffectsHelper;
import net.minecraft.client.gui.screens.WinScreen;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

//When player enters portal after end dragon killed
@RegisterVRDecorator
public class DecoratorWinScreen extends VRDecorator {
    public static final String ID = "win_screen";



    public DecoratorWinScreen(@NotNull VisorAddon owner) {
        super(owner, ID);
    }


    @Override
    public void tick() {

    }

    @Override
    public void render(PoseStack poseStack, float partialTicks) {
        //For now its only have this effect.
        VREffectsHelper.renderInBlockEffect();
        //planned to make it much more cool...

        MC.gameRenderer.lightTexture().turnOffLightLayer();


        ClientContext.guiManager.renderGUI(poseStack, partialTicks);

        ClientContext.handRenderer.renderGuiHands(
                this,
                poseStack, partialTicks,
                true, true
        );


        ClientContext.decorationRenderer.renderGameEffects(
                this,
                poseStack, partialTicks
        );
    }


    @Override
    public boolean canActivate() {
        return MC.level != null
                && MC.screen instanceof WinScreen;
    }

    @Override
    public List<String> gameEffects() {
        return List.of();
    }

    @Override
    public List<String> handEffects() {
        return List.of();
    }

    @Override
    public @NotNull ComponentPriority getPriority() {
        return ComponentPriority.NORMAL;
    }

}
