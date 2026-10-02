package org.vmstudio.visor.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.vmstudio.visor.core.client.tasks.types.TaskRoomConsume;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MinecraftInputMixin {


    /* ***************** *\
  //--------MIXINS--------\\
    \* ***************** */

    /**
     * Overrides an action performed when
     * pressed "keyTogglePerspective" button
     * <br>
     * So, instead this button changes mirror camera type
     *
     * @param instance   s
     * @param cameraType s
     */
    @WrapOperation(at = @At(value = "INVOKE", target ="Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V"), method = "handleKeybinds")
    public void visor$toggleMirrorButton(Options instance, CameraType cameraType, Operation<Void> original) {
        if (VisorState.get().isActive()) {
            ClientContext.settingsManager.nextOptionValue(
                    VROptionWidgetType.MIRROR_MODE.getKey()
            );
        } else {
            original.call(instance, cameraType);
        }
    }

    /**
     * Disables last method that can be called when
     * pressed "keyTogglePerspective" button
     *
     * @param instance s
     * @param entity   s
     */
    @WrapOperation(at = @At(value = "INVOKE", target ="Lnet/minecraft/client/renderer/GameRenderer;checkEntityPostEffect(Lnet/minecraft/world/entity/Entity;)V"), method = "handleKeybinds")
    public void visor$noTogglePerspectiveAction(GameRenderer instance, Entity entity, Operation<Void> original) {
        if (VisorState.get().isNotActive()) {
            original.call(instance, entity);
        }
    }

    /**
     * Makes mouse always grabbed,
     * since it should not be disabled in VR mode
     *
     * @param instance s
     * @return s
     */
    @WrapOperation(at = @At(value = "INVOKE", target ="Lnet/minecraft/client/MouseHandler;isMouseGrabbed()Z"), method = "handleKeybinds")
    public boolean visor$mouseAlwaysGrabbed(MouseHandler instance, Operation<Boolean> original) {
        return VisorState.get().isActive() || original.call(instance);
    }

    //? if >=26.2 {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Screenshot;grab(Lnet/minecraft/client/Minecraft;Z)V"), method = "handleGlobalKeyPress")
    private void visor$screenshot(Minecraft minecraft, boolean debugPanorama, Operation<Void> original) {
        if (VisorState.get().isNotActive()) {
            original.call(minecraft, debugPanorama);
            return;
        }
        ClientContext.renderer.setAskedForScreenShot(true);
    }
    //?}

    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"))
    private void visor$keepConsume(MultiPlayerGameMode instance, Player player, Operation<Void> original) {
        if (VisorState.get().isActive()
                && TaskRoomConsume.getInstance() != null
                && TaskRoomConsume.getInstance().isGestureConsuming()) {
            return;
        }
        original.call(instance, player);
    }
}
