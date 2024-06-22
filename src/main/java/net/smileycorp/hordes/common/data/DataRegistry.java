package net.smileycorp.hordes.common.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.smileycorp.atlas.api.data.BinaryOperation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.LogicalOperation;
import net.smileycorp.atlas.api.data.UnaryOperation;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.conditions.*;
import net.smileycorp.hordes.common.data.values.*;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.data.functions.FunctionRegistry;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DataRegistry {

	private static Map<ResourceLocation, BiFunction<JsonObject, DataType, ValueGetter>> VALUE_GETTERS = Maps.newHashMap();
	private static Map<ResourceLocation, Function<JsonElement, Condition>> CONDITION_DESERIALIZERS = Maps.newHashMap();

	public static void init() {
		registerValueGetters();
		registerConditionDeserializers();
		if (HordeEventConfig.enableHordeEvent.get()) FunctionRegistry.registerFunctionSerializers();
	}

	private static void registerValueGetters() {
		UnaryOperation.values().forEach(operation -> registerValueGetter(Constants.loc(operation.getName()),
				(obj, type) -> UnaryOperationValueGetter.deserialize(operation, type, obj)));
		BinaryOperation.values().forEach(operation -> registerValueGetter(Constants.loc(operation.getName()),
				(obj, type) -> BinaryOperationValueGetter.deserialize(operation, type, obj)));
		registerValueGetter(Constants.loc("weighted_random"), WeightedRandomValueGetter::deserialize);
		registerValueGetter(Constants.loc("level_nbt"), LevelNBTGetter::deserialize);
		registerValueGetter(Constants.loc("player_nbt"), PlayerNBTGetter::deserialize);
		registerValueGetter(Constants.loc("player_pos"), PlayerPosGetter::deserialize);
		registerValueGetter(Constants.loc("entity_nbt"), EntityNBTGetter::deserialize);
		registerValueGetter(Constants.loc("entity_pos"), EntityPosGetter::deserialize);
	}

	public static void registerConditionDeserializers() {
		for (LogicalOperation operation : LogicalOperation.values())
			registerConditionDeserializer(Constants.loc(operation.getName()), e -> LogicalCondition.deserialize(operation, e));
		registerConditionDeserializer(Constants.loc("not"), NotCondition::deserialize);
		registerConditionDeserializer(Constants.loc("comparison"), ComparisonCondition::deserialize);
		registerConditionDeserializer(Constants.loc("random"), RandomCondition::deserialize);
		registerConditionDeserializer(Constants.loc("biome"), BiomeCondition::deserialize);
		registerConditionDeserializer(Constants.loc("day"), DayCondition::deserialize);
		registerConditionDeserializer(Constants.loc("player_day"), PlayerDayCondition::deserialize);
		registerConditionDeserializer(Constants.loc("local_difficulty"), LocalDifficultyCondition::deserialize);
		registerConditionDeserializer(Constants.loc("game_difficulty"), GameDifficultyCondition::deserialize);
		registerConditionDeserializer(Constants.loc("advancement"), AdvancementCondition::deserialize);
		registerConditionDeserializer(Constants.loc("entity_type"), EntityTypeCondition::deserialize);
		if (ModList.get().isLoaded("gamestages")) registerConditionDeserializer(ResourceLocation.tryParse("gamestages:gamestage"), GameStagesCondition::deserialize);
	}

	public static ValueGetter readValue(DataType type, JsonObject json) {
		if (json.has("name") && json.has("value")) {
			try {
				ResourceLocation loc = ResourceLocation.tryParse(json.get("name").getAsString());
				BiFunction<JsonObject, DataType, ValueGetter> getter = VALUE_GETTERS.get(loc);
				if (getter == null) throw new NullPointerException("value getter " + loc + " is not registered");
				return getter.apply(json, type);
			} catch (Exception e) {
				HordesLogger.logError("Failed to read value " + json, e);
			}
		}
		return null;
	}

	public static Condition readCondition(JsonObject json) {
		if (json.has("name") && json.has("value")) {
			try {
				ResourceLocation loc = new ResourceLocation(json.get("name").getAsString());
				Function<JsonElement, Condition> deserializer = CONDITION_DESERIALIZERS.get(loc);
				if (deserializer == null) throw new NullPointerException("condition " + loc + " is not registered");
				return deserializer.apply(json.get("value"));
			} catch (Exception e) {
				HordesLogger.logError("Failed to read condition " + json, e);
			}
		}
		return null;
	}

	public static void registerValueGetter(ResourceLocation name, BiFunction<JsonObject, DataType, ValueGetter> getter) {
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
