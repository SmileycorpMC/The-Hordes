package net.smileycorp.hordes.hordeevent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import net.smileycorp.atlas.api.util.Func;

import java.util.function.BiPredicate;

public enum HordeSpawnType {
    
    AVOID_FLUIDS(((level, pos) -> level.getBlockState(pos.below()).getFluidState().isEmpty())),
    PREFER_WATER((level, pos) -> level.getBlockState(pos.below()).getFluidState().is(FluidTags.WATER)),
    PREFER_LAVA((level, pos) -> level.getBlockState(pos.below()).getFluidState().is(FluidTags.LAVA)),
    IGNORE_WATER((level, pos) -> {
        FluidState state = level.getBlockState(pos.below()).getFluidState();
        return state.isEmpty() || state.is(FluidTags.WATER);
    }),
    IGNORE_FLUIDS(Func::True);
    
    private final BiPredicate<ServerLevel, BlockPos> canSpawn;
    
    HordeSpawnType(BiPredicate<ServerLevel, BlockPos> canSpawn) {
        this.canSpawn = canSpawn;
    }
    
    public boolean canSpawn(ServerLevel level, BlockPos pos) {
        return canSpawn.test(level, pos);
    }
    
}
