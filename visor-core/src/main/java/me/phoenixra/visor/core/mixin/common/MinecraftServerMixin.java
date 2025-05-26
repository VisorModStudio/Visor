package me.phoenixra.visor.core.mixin.common;

import com.mojang.datafixers.DataFixer;
import me.phoenixra.visor.core.server.VisorServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    protected abstract boolean initServer() throws IOException;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void visor$initServer(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory,
                                CallbackInfo callbackInfo){
        new VisorServer();
    }
    @Inject(at = @At("TAIL"), method = "stopServer")
    public void visor$stopServer(CallbackInfo callbackInfo){
        VisorServer.INSTANCE.onServerStop();
    }

    @Inject(at = @At("HEAD"), method = "tickServer")
    public void visor$tickVR(BooleanSupplier booleanSupplier,
                            CallbackInfo callbackInfo){
        VisorServer.INSTANCE.tickVR();
    }


}
