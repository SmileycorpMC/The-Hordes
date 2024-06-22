package net.smileycorp.hordes.hordeevent;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.smileycorp.hordes.common.HordesLogger;

import java.util.List;

public class CustomSpawnType implements HordeSpawnType{
   
   private final List<Either<TagKey<Block>, Block>> blocks = Lists.newArrayList();
    
    public CustomSpawnType(List<String> strings) {
        for (String str : strings) try {
            blocks.add(str.contains("#") ? Either.left(TagKey.create(Registries.BLOCK, ResourceLocation.tryParse((str.replace("#", "")))))
                    : Either.right(BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(str))));
        } catch (Exception e) {
            HordesLogger.logError("Failed parsing block " + str, e);
        }
    }
    
    @Override
    public boolean canSpawn(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos.below());
        for (Either<TagKey<Block>, Block> either : blocks) if (either.map(state::is, state::is)) return true;
        return false;
    }
    
    public ListTag toNbt() {
        ListTag tag = new ListTag();
        blocks.forEach(either -> tag.add(StringTag.valueOf(either.map(t -> "#" + t.location(), b -> BuiltInRegistries.BLOCK.getKey(b).toString()))));
        return tag;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName()+"[");
       blocks.forEach(either -> builder.append(either.map(t -> "#" + t.location(), b -> BuiltInRegistries.BLOCK.getKey(b).toString()) + ", "));
        return builder.append("]").toString();
    }
    
}
