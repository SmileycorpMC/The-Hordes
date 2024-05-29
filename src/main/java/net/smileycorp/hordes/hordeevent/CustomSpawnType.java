package net.smileycorp.hordes.hordeevent;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.Either;

import java.util.List;

public class CustomSpawnType implements HordeSpawnType{
   
   private final List<Either<String, Block>> blocks = Lists.newArrayList();
    
    public CustomSpawnType(List<String> strings) {
        for (String str : strings) try {
            blocks.add(str.contains("#") ? Either.left(str.replace("#", ""))
                    : Either.right(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(str))));
        } catch (Exception e) {
            HordesLogger.logError("Failed parsing block " + str, e);
        }
    }
    
    @Override
    public boolean canSpawn(World level, BlockPos pos) {
        IBlockState state = level.getBlockState(pos.down());
        for (Either<String, Block> either : blocks) if (either.map(
                l -> {
                    int id = OreDictionary.getOreID(l);
                    for (int i : OreDictionary.getOreIDs(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)))) if (id == i) return true;
                    return false;
                },
                r -> state.getBlock() == r)) return true;
        return false;
    }
    
    public NBTTagList toNbt() {
        NBTTagList tag = new NBTTagList();
        for (Either<String, Block> either : blocks) tag.appendTag(new NBTTagString(either.map(t -> "#" + t, b -> ForgeRegistries.BLOCKS.getKey(b).toString())));
        return tag;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName()+"[");
        for (Either<String, Block> either : blocks) builder.append(either.map(t -> "#" + t, b -> ForgeRegistries.BLOCKS.getKey(b).toString()) + ", ");
        return builder.append("]").toString();
    }
    
}
