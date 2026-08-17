package org.vmstudio.visor.mixin.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.extensions.client.entity.EntityRenderStateExtension;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateExtension {

    @Unique
    @Nullable
    private VRClientPlayer visor$vrPlayer;

    @Unique
    private boolean visor$selfModelRender;

    @Unique
    private boolean visor$selfModelPlayer;

    @Unique
    private boolean visor$selfModelHandsRender;

    @Override
    @Nullable
    public VRClientPlayer visor$getVRPlayer() {
        return this.visor$vrPlayer;
    }

    @Override
    public void visor$setVRPlayer(@Nullable VRClientPlayer vrPlayer) {
        this.visor$vrPlayer = vrPlayer;
    }

    @Override
    public boolean visor$isSelfModelRender() {
        return this.visor$selfModelRender;
    }

    @Override
    public void visor$setSelfModelRender(boolean selfModelRender) {
        this.visor$selfModelRender = selfModelRender;
    }

    @Override
    public boolean visor$isSelfModelPlayer() {
        return this.visor$selfModelPlayer;
    }

    @Override
    public void visor$setSelfModelPlayer(boolean selfModelPlayer) {
        this.visor$selfModelPlayer = selfModelPlayer;
    }

    @Override
    public boolean visor$isSelfModelHandsRender() {
        return this.visor$selfModelHandsRender;
    }

    @Override
    public void visor$setSelfModelHandsRender(boolean selfModelHandsRender) {
        this.visor$selfModelHandsRender = selfModelHandsRender;
    }
}
