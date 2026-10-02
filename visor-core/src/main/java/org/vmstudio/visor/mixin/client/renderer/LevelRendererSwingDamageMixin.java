package org.vmstudio.visor.mixin.client.renderer;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.extensions.client.render.LevelRendererExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

// better swinging
//? if >=26.2 {
@Mixin(value = ClientLevel.class, priority = 999)
//?} else {
/*@Mixin(value = LevelRenderer.class, priority = 999)
*///?}
public abstract class LevelRendererSwingDamageMixin implements LevelRendererExtension {

    // ---- Shadow fields ----
    @Final @Shadow
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;
    @Final @Shadow
    private Int2ObjectMap<BlockDestructionProgress> destroyingBlocks;

    // ---- Unique fields ----
    @Unique
    private Map<Long, Long> visor$damagedBlocksVr;
    @Unique
    private Map<Long, BlockDestructionProgress> visor$damagedBlocksVrSave;


    /* ***************** *\
  //--------MIXINS--------\\
    \* ***************** */
    // CallbackInfo only: the LevelRenderer constructor gained arguments in 1.21.9
    @Inject(method = "<init>", at = @At("RETURN"))
    private void visor$initFields(CallbackInfo ci) {
        visor$damagedBlocksVr = Collections.synchronizedMap(new HashMap<>());
        visor$damagedBlocksVrSave = Collections.synchronizedMap(new HashMap<>());
    }

    //? if <26.2 {
    /*@Inject(at = @At("HEAD"), method = "setLevel")
    private void visor$clearSwingDamage(ClientLevel level, CallbackInfo ci) {
        visor$damagedBlocksVrSave.keySet().forEach(
                key -> destructionProgress.remove(key.longValue())
        );
        visor$damagedBlocksVr.clear();
        visor$damagedBlocksVrSave.clear();
    }
    *///?}

    @Inject(at = @At("HEAD"), method = "removeProgress", cancellable = true)
    private void visor$removeProgress(BlockDestructionProgress progress,
                                      CallbackInfo ci
    ) {
        //fix of crash bcz of vr swinging
        ci.cancel();
        long blockPos = progress.getPos().asLong();
        Set<BlockDestructionProgress> set = this.destructionProgress.get(blockPos);
        if (set == null) return; //here it is
        set.remove(progress);
        if (set.isEmpty()) {
            this.destructionProgress.remove(blockPos);
        }

    }

    //? if >=26.2 {
    @Inject(at = @At("HEAD"), method = "update")
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "renderLevel")
    *///?}
    private void visor$betterSwinging(CallbackInfo ci) {
        if (visor$damagedBlocksVr.isEmpty()
                && visor$damagedBlocksVrSave.isEmpty()) {
            return;
        }

        try {

            List<Long> toRemove = new ArrayList<>();
            destructionProgress.forEach((key, value) -> {
                int stage = value.last().getProgress();
                if (stage < 0 || stage >= ModelBakery.DESTROY_TYPES.size()) {
                    toRemove.add(key);
                }
            });
            toRemove.forEach(it -> {
                destructionProgress.remove(it.longValue());
                visor$damagedBlocksVr.remove(it);
                visor$damagedBlocksVrSave.remove(it);
            });
            toRemove.clear();
            for (Map.Entry<Long, Long> entry : visor$damagedBlocksVr.entrySet()) {
                SortedSet<BlockDestructionProgress> set = destructionProgress.get(entry.getKey());
                if (set == null) {
                    toRemove.add(entry.getKey());
                    continue;
                }
                BlockDestructionProgress d = visor$damagedBlocksVrSave.get(entry.getKey());
                if (d == null) {
                    toRemove.add(entry.getKey());
                    continue;
                }
                if (!set.contains(d) || set.size() > 1) {
                    toRemove.add(entry.getKey());
                    continue;
                }
                //if anything happened with packet from server
                if (entry.getValue() + (VRServerSettings.getSwingingRepairDelay() * 50)
                        < System.currentTimeMillis()) {
                    toRemove.add(entry.getKey());
                }
            }
            toRemove.forEach(it -> {
                destructionProgress.remove(it.longValue());
                visor$damagedBlocksVr.remove(it);
                visor$damagedBlocksVrSave.remove(it);
            });
        }catch(Throwable e){
            LoggerUtils.printError(e);
        }
    }


    /* ************************ *\
  //--------PUBLIC METHODS--------\\
    \* ************************ */

    @Override
    @Unique
    public void visor$damageBlockProgress(@NotNull Player player,
                                          @NotNull BlockPos blockPos,
                                          int destroyStage
    ) {
        if (!VRServerSettings.isBetterSwinging()
                || VisorState.get().isNotActive()) return;

        if (destroyStage == -1) {
            visor$damagedBlocksVr.remove(blockPos.asLong());
            visor$damagedBlocksVrSave.remove(blockPos.asLong());
            destructionProgress.remove(blockPos.asLong());
            return;
        }

        if (destroyStage == -2) {
            visor$damagedBlocksVr.remove(blockPos.asLong());
            visor$damagedBlocksVrSave.remove(blockPos.asLong());
            return;
        }

        final List<Integer> toRemove = new ArrayList<>();

        destroyingBlocks.forEach((id, progress) -> {
            if (progress.getPos().asLong() == blockPos.asLong()) {
                toRemove.add(id);
                destructionProgress.remove(progress.getPos().asLong());
            }
        });

        toRemove.forEach(it -> destroyingBlocks.remove(it.intValue()));

        BlockDestructionProgress progress = new BlockDestructionProgress(
                player.getId(), blockPos
        );
        progress.setProgress(destroyStage);

        SortedSet<BlockDestructionProgress> set =
                destructionProgress.computeIfAbsent(
                        progress.getPos().asLong(), (p_234254_) -> {
                            return Sets.newTreeSet();
                        }
                );

        set.clear();
        set.add(progress);

        visor$damagedBlocksVr.put(blockPos.asLong(), System.currentTimeMillis());
        visor$damagedBlocksVrSave.put(blockPos.asLong(), progress);
    }
}
