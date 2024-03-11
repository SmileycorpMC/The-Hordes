package net.smileycorp.hordes.hordeevent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface HordeSpawnType {
    
    boolean canSpawn(ServerLevel level, BlockPos pos);
    
}
