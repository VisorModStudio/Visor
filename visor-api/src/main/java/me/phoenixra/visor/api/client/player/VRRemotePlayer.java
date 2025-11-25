package me.phoenixra.visor.api.client.player;

import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3fc;

public interface VRRemotePlayer extends VRClientPlayer{
    RemotePlayer getMcPlayer();



}
