package net.smileycorp.hordes.hordeevent;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tags.FluidTags;
import net.smileycorp.hordes.common.HordesLogger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HordeSpawnTypes {
    
    private static final Map<String, HordeSpawnType> SPAWN_TYPES = Maps.newHashMap();
    
    public static final HordeSpawnType AVOID_FLUIDS = register("avoid_fluids", ((level, pos) -> level.getBlockState(pos.below()).getFluidState().isEmpty()));
    public static final HordeSpawnType PREFER_WATER = register("prefer_water", (level, pos) -> level.getBlockState(pos.below()).getFluidState().is(FluidTags.WATER));
    public static final HordeSpawnType PREFER_LAVA = register("prefer_lava", (level, pos) -> level.getBlockState(pos.below()).getFluidState().is(FluidTags.LAVA));
    public static final HordeSpawnType IGNORE_WATER = register("ignore_water", (level, pos) -> {
        FluidState state = level.getBlockState(pos.below()).getFluidState();
        return state.isEmpty() || state.is(FluidTags.WATER);
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
    
    public static INBT toNbt(HordeSpawnType spawnType) {
        if (spawnType instanceof CustomSpawnType) return ((CustomSpawnType)spawnType).toNbt();
        for (Map.Entry<String, HordeSpawnType> entry : SPAWN_TYPES.entrySet()) if (entry.getValue().equals(spawnType)) return StringNBT.valueOf(entry.getKey());
        return null;
    }
    
    public static HordeSpawnType fromNBT(INBT tag) {
        if (tag instanceof StringNBT) {
            HordeSpawnType type = SPAWN_TYPES.get(tag.getAsString());
           if (type != null) return type;
        } else if (tag instanceof ListNBT) {
            try {
                List<String> strings = ((ListNBT) tag).stream().map(INBT::getAsString).collect(Collectors.toList());
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
