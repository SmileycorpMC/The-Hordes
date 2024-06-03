package net.smileycorp.hordes.hordeevent;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fluids.IFluidBlock;
import net.smileycorp.hordes.common.HordesLogger;

import java.util.List;
import java.util.Map;

public class HordeSpawnTypes {
    
    private static final Map<String, HordeSpawnType> SPAWN_TYPES = Maps.newHashMap();
    
    public static final HordeSpawnType AVOID_FLUIDS = register("avoid_fluids", ((level, pos) ->
            !(level.getBlockState(pos.down()).getBlock() instanceof IFluidBlock)));
    public static final HordeSpawnType PREFER_WATER = register("prefer_lava", (level, pos) -> {
        IBlockState state = level.getBlockState(pos.down());
        return state instanceof IFluidBlock && ((IFluidBlock)state.getBlock()).getFluid().getTemperature() <= 300;
    });
    public static final HordeSpawnType PREFER_LAVA = register("prefer_lava", (level, pos) -> {
        IBlockState state = level.getBlockState(pos.down());
        return state instanceof IFluidBlock && ((IFluidBlock)state.getBlock()).getFluid().getTemperature() > 500;
    });
    public static final HordeSpawnType IGNORE_WATER = register("ignore_water", (level, pos) -> {
        IBlockState state = level.getBlockState(pos.down());
        if (!(state instanceof IFluidBlock)) return true;
        return ((IFluidBlock)state.getBlock()).getFluid().getTemperature() <= 300;
    });
    public static final HordeSpawnType IGNORE_FLUIDS = register("ignore_fluids", (level, pos) -> true);
    
    public static HordeSpawnType register(String name, HordeSpawnType spawnType) {
        SPAWN_TYPES.put(name, spawnType);
        return spawnType;
    }
    
    public static String toString(HordeSpawnType spawnType) {
        if (spawnType instanceof CustomSpawnType) return spawnType.toString();
        for (Map.Entry<String, HordeSpawnType> entry : SPAWN_TYPES.entrySet()) if (entry.getValue().equals(spawnType)) return entry.getKey();
        return null;
    }
    
    public static NBTBase toNbt(HordeSpawnType spawnType) {
        if (spawnType instanceof CustomSpawnType) return ((CustomSpawnType)spawnType).toNbt();
        for (Map.Entry<String, HordeSpawnType> entry : SPAWN_TYPES.entrySet()) if (entry.getValue().equals(spawnType)) return new NBTTagString(entry.getKey());
        return null;
    }
    
    public static HordeSpawnType fromNBT(NBTBase tag) {
        if (tag instanceof NBTTagString) {
            HordeSpawnType type = SPAWN_TYPES.get(((NBTTagString)tag).getString());
           if (type != null) return type;
        } else if (tag instanceof NBTTagList) {
            try {
                List<String> strings = Lists.newArrayList();
                for (NBTBase nbt : ((NBTTagList)tag)) strings.add(((NBTTagString)nbt).getString());
                return new CustomSpawnType(strings);
            } catch (Exception e) {
                HordesLogger.logError("Failed reading nbt " + tag, e);
            }
        }
        return AVOID_FLUIDS;
    }
    
    public static HordeSpawnType fromJson(JsonElement tag) {
        if (tag instanceof JsonPrimitive) {
            HordeSpawnType type = SPAWN_TYPES.get(tag.getAsString());
            if (type != null) return type;
        } else if (tag instanceof JsonArray) {
            try {
                List<String> strings = Lists.newArrayList();
                ((JsonArray) tag).forEach(e -> strings.add(e.getAsString()));
                return new CustomSpawnType(strings);
            } catch (Exception e) {
                HordesLogger.logError("Failed reading nbt " + tag, e);
            }
        }
        return AVOID_FLUIDS;
    }
    
}
