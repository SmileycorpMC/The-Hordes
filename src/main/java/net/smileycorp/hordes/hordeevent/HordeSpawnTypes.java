package net.smileycorp.hordes.hordeevent;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.atlas.api.util.Func;
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
    public static final HordeSpawnType IGNORE_FLUIDS = register("ignore_fluids", Func::True);
    
    public static HordeSpawnType register(String name, HordeSpawnType spawnType) {
        return SPAWN_TYPES.put(name, spawnType);
    }
    
    public static HordeSpawnType fromNBT(Tag tag) {
        if (tag instanceof StringTag) {
            HordeSpawnType type = SPAWN_TYPES.get(tag.getAsString());
           if (type != null) return type;
        } else if (tag instanceof ListTag) {
            try {
                List<String> strings = ((ListTag) tag).stream().map(Tag::getAsString).collect(Collectors.toList());
                return custom(strings);
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
                List<String> strings = ((JsonArray) tag).asList().stream().map(JsonElement::getAsString).collect(Collectors.toList());
                return custom(strings);
            } catch (Exception e) {
                HordesLogger.logError("Failed reading nbt " + tag, e);
            }
        }
        return AVOID_FLUIDS;
    }
    
    private static HordeSpawnType custom(List<String> strings) {
        return ((level, pos) -> {
            BlockState state = level.getBlockState(pos.below());
            for (String string : strings) {
                try {
                    if (string.contains("#")) {
                        if (state.is(TagKey.create(Registries.BLOCK, new ResourceLocation(string.replace("#", ""))))) return true;
                    } else {
                        if (state.is(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string)))) return true;
                    }
                } catch (Exception e) {
                    HordesLogger.logError("Failed logging parameter " + string, e);
                }
            }
            return false;
        });
    }
    
}
