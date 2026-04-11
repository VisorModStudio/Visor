package org.vmstudio.visor.compatibility.immptl;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public record PortalAwareControllerHit(
        Level world,
        BlockHitResult hitResult,
        List<Object> portalsPassingThrough
) {
}