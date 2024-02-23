package net.smileycorp.hordes.common.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.LogicalOperation;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.conditions.*;
import net.smileycorp.hordes.common.data.values.EntityNBTGetter;
import net.smileycorp.hordes.common.data.values.EntityPosGetter;
import net.smileycorp.hordes.common.data.values.LevelNBTGetter;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.hordeevent.data.functions.FunctionRegistry;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DataRegistry {

	private static Map<ResourceLocation, BiFunction<String, DataType, ValueGetter>> VALUE_GETTERS = Maps.newHashMap();
	private static Map<ResourceLocation, Function<JsonElement, Condition>> CONDITION_DESERIALIZERS = Maps.newHashMap();

	public static void init() {
		registerValueGetters();
		registerConditionDeserializers();
		if (CommonConfigHandler.enableHordeEvent.get()) FunctionRegistry.registerFunctionSerializers();
	}

	private static void registerValueGetters() {
		registerValueGetter(Constants.loc("level_nbt"), LevelNBTGetter::new);
		registerValueGetter(Constants.loc("player_nbt"), EntityNBTGetter::new);
		registerValueGetter(Constants.loc("player_pos"), EntityPosGetter::new);
	}


	public static void registerConditionDeserializers() {
		for (LogicalOperation operation : LogicalOperation.values())
			registerConditionDeserializer(Constants.loc(operation.getName()), e -> LogicalCondition.deserialize(operation, e));
		registerConditionDeserializer(Constants.loc("comparison"), ComparisonCondition::deserialize);
		registerConditionDeserializer(Constants.loc("random"), RandomCondition::deserialize);
		registerConditionDeserializer(Constants.loc("biome"), BiomeCondition::deserialize);
		registerConditionDeserializer(Constants.loc("day"), DayCondition::deserialize);
		if (ModList.get().isLoaded("gamestages")) registerConditionDeserializer(Constants.loc("gamestage"), GameStagesCondition::deserialize);
	}

	public static ValueGetter readValue(DataType type, JsonObject json) {
		if (json.has("name") && json.has("value")) {
			try {
				return VALUE_GETTERS.get(new ResourceLocation(json.get("name").getAsString()))
						.apply(json.get("value").getAsString(), type);
			} catch (Exception e) {
				HordesLogger.logError("Failed to read value " + json, e);
			}
		}
		return null;
	}

	public static Condition readCondition(JsonObject json) {
		if (json.has("name") && json.has("value")) {
			try {
				return CONDITION_DESERIALIZERS.get(new ResourceLocation(json.get("name").getAsString())).apply(json.get("value"));
			} catch (Exception e) {
				HordesLogger.logError("Failed to read condition " + json, e);
			}
		}
		return null;
	}

	public static void registerValueGetter(ResourceLocation name, BiFunction<String, DataType, ValueGetter> getter) {
		VALUE_GETTERS.put(name, getter);
	}

	public static void registerConditionDeserializer(ResourceLocation name, Function<JsonElement, Condition> serializer) {
		CONDITION_DESERIALIZERS.put(name, serializer);
	}

    public static CompoundTag parseNBT(String name, String nbtstring) {
        CompoundTag nbt = null;
        try {
            CompoundTag parsed = TagParser.parseTag(nbtstring);
            if (parsed != null) nbt = parsed;
            else throw new NullPointerException("Parsed NBT is null.");
        } catch (Exception e) {
            HordesLogger.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
            HordesLogger.logError("Error parsing nbt for entity " + name + " " + e.getMessage(), e);
        }
        return nbt;
    }
}
