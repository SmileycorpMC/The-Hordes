package net.smileycorp.hordes.hordeevent;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.hordes.common.HordesLogger;

import java.util.List;

public class CustomSpawnType implements HordeSpawnType{
   
   private final List<Either<Tags.IOptionalNamedTag<Block>, Block>> blocks = Lists.newArrayList();
    
    public CustomSpawnType(List<String> strings) {
        for (String str : strings) try {
            blocks.add(str.contains("#") ? Either.left(ForgeTagHandler.createOptionalTag(Registry.BLOCK_REGISTRY.getRegistryName(), new ResourceLocation(str.replace("#", ""))))
                    : Either.right(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(str))));
        } catch (Exception e) {
            HordesLogger.logError("Failed parsing block " + str, e);
        }
    }
    
    @Override
    public boolean canSpawn(World level, BlockPos pos) {
        BlockState state = level.getBlockState(pos.below());
        for (Either<Tags.IOptionalNamedTag<Block>, Block> either : blocks) if (either.map(state::is, state::is)) return true;
        return false;
    }
    
    public ListNBT toNbt() {
        ListNBT tag = new ListNBT();
        for (Either<Tags.IOptionalNamedTag<Block>, Block> either : blocks) tag.add(StringNBT.valueOf(either.map(t -> "#" + t.getName(), b -> ForgeRegistries.BLOCKS.getKey(b).toString())));
        return tag;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName()+"[");
        for (Either<Tags.IOptionalNamedTag<Block>, Block> either : blocks) {
            builder.append(either.map(t -> "#" + t.getName(), b -> ForgeRegistries.BLOCKS.getKey(b).toString()) + ", ");
        }
        return builder.append("]").toString();
    }
    
}
