package org.vmstudio.visor.extensions.common;

import net.minecraft.world.entity.Entity;
import org.vmstudio.visor.api.common.HandType;

public interface PlayerExtension {
    void visor$swingAttack(Entity entity, HandType handType);
}
