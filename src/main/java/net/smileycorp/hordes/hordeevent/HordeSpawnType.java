package net.smileycorp.hordes.hordeevent;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface HordeSpawnType {
    
    boolean canSpawn(World level, BlockPos pos);
    
}
