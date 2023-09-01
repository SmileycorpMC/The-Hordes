package net.smileycorp.hordes.common.hordeevent.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.LogicalOperation;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.data.scripts.conditions.*;
import net.smileycorp.hordes.common.hordeevent.data.scripts.values.LevelNBTGetter;
import net.smileycorp.hordes.common.hordeevent.data.scripts.values.PlayerNBTGetter;
import net.smileycorp.hordes.common.hordeevent.data.scripts.values.PlayerPosGetter;
import net.smileycorp.hordes.common.hordeevent.data.scripts.values.ValueGetter;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class HordeDataRegistry {

	private static Map<ResourceLocation, BiFunction<String, DataType, ValueGetter>> VALUE_GETTERS = Maps.newHashMap();
	private static Map<ResourceLocation, Function<JsonElement, Condition>> CONDITION_DESERIALIZERS = Maps.newHashMap();

	public static void init() {
		registerValueGetters();
		registerDeserializers();
		registerFunctions();
	}

	private static void registerValueGetters() {
		registerValueGetter(Constants.loc("level_nbt"), LevelNBTGetter::new);
		registerValueGetter(Constants.loc("player_nbt"), PlayerNBTGetter::new);
		registerValueGetter(Constants.loc("player_pos"), PlayerPosGetter::new);
	}


	public static void registerDeserializers() {
		for (LogicalOperation operation : LogicalOperation.values())
			registerConditionDeserializer(Constants.loc(operation.getName()), e -> LogicalCondition.deserialize(operation, e));
		registerConditionDeserializer(Constants.loc("comparison"), ComparisonCondition::deserialize);
		registerConditionDeserializer(Constants.loc("random"), RandomCondition::deserialize);
		registerConditionDeserializer(Constants.loc("biome"), BiomeCondition::deserialize);
		registerConditionDeserializer(Constants.loc("day"), DayCondition::deserialize);
	}

	public static void registerFunctions() {
		//TODO: add function deserializers
	}

	public static ValueGetter readValue(DataType type, JsonObject json) {
		if (json.has("name") && json.has("value")) {
			try {
				return VALUE_GETTERS.get(new ResourceLocation(json.get("name").getAsString()))
						.apply(json.get("value").getAsString(), type);
			} catch (Exception e) {
				Hordes.logError("Failed to read value "+ json, e);
			}
		}
		return null;
	}

	public static Condition readCondition(JsonObject json) {
		if (json.has("name") && json.has("value")) {
			try {
				return CONDITION_DESERIALIZERS.get(new ResourceLocation(json.get("name").getAsString())).apply(json.get("value"));
			} catch (Exception e) {
				Hordes.logError("Failed to read condition "+ json, e);
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

}
