package net.smileycorp.hordes.common.hordeevent.data;

import com.google.common.collect.Maps;
import com.google.gson.*;

import net.minecraft.resources.ResourceLocation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.LogicalOperation;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.hordeevent.data.conditions.*;
import net.smileycorp.hordes.common.hordeevent.data.values.LevelNBTGetter;
import net.smileycorp.hordes.common.hordeevent.data.values.PlayerNBTGetter;
import net.smileycorp.hordes.common.hordeevent.data.values.PlayerPosGetter;
import net.smileycorp.hordes.common.hordeevent.data.values.ValueGetter;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DataRegistry {

	private static Map<ResourceLocation, BiFunction<String, DataType, ValueGetter>> VALUE_GETTERS = Maps.newHashMap();
	private static Map<ResourceLocation, Function<JsonElement, Condition>> CONDITION_DESERIALIZERS = Maps.newHashMap();

	public static void init() {
		registerValueGetters();
		registerDeserializers();
	}

	private static void registerValueGetters() {
		registerValueGetter(ModDefinitions.getResource("level_nbt"), LevelNBTGetter::new);
		registerValueGetter(ModDefinitions.getResource("player_nbt"), PlayerNBTGetter::new);
		registerValueGetter(ModDefinitions.getResource("player_pos"), PlayerPosGetter::new);
	}


	public static void registerDeserializers() {
		for (LogicalOperation operation : LogicalOperation.values())
			registerConditionDeserializer(ModDefinitions.getResource(operation.getName()), e -> LogicalCondition.deserialize(operation, e));
		registerConditionDeserializer(ModDefinitions.getResource("comparison"), ComparisonCondition::deserialize);
		registerConditionDeserializer(ModDefinitions.getResource("random"), RandomCondition::deserialize);
		registerConditionDeserializer(ModDefinitions.getResource("biome"), BiomeCondition::deserialize);
		registerConditionDeserializer(ModDefinitions.getResource("day"), DayCondition::deserialize);
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
