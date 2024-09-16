package net.smileycorp.hordes.hordeevent.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
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
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.common.event.HordeSpawnEntityEvent;
import net.smileycorp.hordes.hordeevent.data.conditions.*;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;
import net.smileycorp.hordes.hordeevent.data.functions.MultipleFunction;
import net.smileycorp.hordes.hordeevent.data.functions.spawndata.*;
import net.smileycorp.hordes.hordeevent.data.functions.spawnentity.*;
import net.smileycorp.hordes.hordeevent.data.values.*;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DataRegistry {

	private static Map<ResourceLocation, BiFunction<JsonObject, DataType, ValueGetter>> VALUE_GETTERS = Maps.newHashMap();
	private static Map<ResourceLocation, Function<JsonElement, Condition>> CONDITIONS = Maps.newHashMap();
	private static Map<ResourceLocation, Pair<Class<? extends HordePlayerEvent>, Function<JsonElement, HordeFunction<? extends HordePlayerEvent>>>> FUNCTIONS = Maps.newHashMap();
	
	public static void init() {
		registerValueGetters();
		registerConditionDeserializers();
		registerFunctionSerializers();
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
		registerValueGetter(Constants.loc("day"), EventDayGetter::deserialize);
		registerValueGetter(Constants.loc("spawn_table"), SpawnTableGetter::deserialize);
	}

	public static void registerConditionDeserializers() {
		for (LogicalOperation operation : LogicalOperation.values())
			registerConditionDeserializer(Constants.loc(operation.getName()), e -> LogicalCondition.deserialize(operation, e));
		registerConditionDeserializer(Constants.loc("not"), NotCondition::deserialize);
		registerConditionDeserializer(Constants.loc("comparison"), ComparisonCondition::deserialize);
		registerConditionDeserializer(Constants.loc("random"), RandomCondition::deserialize);
		registerConditionDeserializer(Constants.loc("biome"), BiomeCondition::deserialize);
		registerConditionDeserializer(Constants.loc("day"), DayCondition::deserialize);
		registerConditionDeserializer(Constants.loc("player_day"), DayCondition::deserialize);
		registerConditionDeserializer(Constants.loc("local_difficulty"), LocalDifficultyCondition::deserialize);
		registerConditionDeserializer(Constants.loc("game_difficulty"), GameDifficultyCondition::deserialize);
		registerConditionDeserializer(Constants.loc("advancement"), AdvancementCondition::deserialize);
		registerConditionDeserializer(Constants.loc("entity_type"), EntityTypeCondition::deserialize);
		if (ModList.get().isLoaded("gamestages")) registerConditionDeserializer(ResourceLocation.tryParse("gamestages:gamestage"), GameStagesCondition::deserialize);
	}
	
	public static void registerFunctionSerializers() {
		//build spawndata functions
		registerFunctionDeserializer(Constants.loc("set_spawntable"), HordeBuildSpawnDataEvent.class, SetSpawntableFunction::deserialize);
		registerFunctionDeserializer(Constants.loc("set_spawn_type"), HordeBuildSpawnDataEvent.class, SetSpawnTypeFunction::deserialize);
		registerFunctionDeserializer(Constants.loc("set_spawn_sound"), HordeBuildSpawnDataEvent.class, SetSpawnSoundFunction::deserialize);
		registerFunctionDeserializer(Constants.loc("set_start_message"), HordeBuildSpawnDataEvent.class, SetStartMessageFunction::deserialize);
		registerFunctionDeserializer(Constants.loc("set_end_message"), HordeBuildSpawnDataEvent.class, SetEndMessageFunction::deserialize);
		registerFunctionDeserializer(Constants.loc("set_spawn_duration"), HordeBuildSpawnDataEvent.class, SetSpawnDurationFunction::deserialize);
		registerFunctionDeserializer(Constants.loc("set_spawn_interval"), HordeBuildSpawnDataEvent.class, SetSpawnIntervalFunction::deserialize);
		registerFunctionDeserializer(Constants.loc("set_spawn_amount"), HordeBuildSpawnDataEvent.class, SetSpawnAmountFunction::deserialize);
		registerFunctionDeserializer(Constants.loc("set_entity_speed"), HordeBuildSpawnDataEvent.class, SetEntitySpeedFunction::deserialize);
		registerFunctionDeserializer(Constants.loc("add_reward_command"), HordeBuildSpawnDataEvent.class, AddRewardCommandFunction::deserialize);
		//spawn entity functions
		registerFunctionDeserializer(Constants.loc("set_entity_type"), HordeSpawnEntityEvent.class, SetEntityTypeFunction::deserialize);
		registerFunctionDeserializer(Constants.loc("set_entity_nbt"), HordeSpawnEntityEvent.class, SetEntityNBTFunction::deserialize);
		registerFunctionDeserializer(Constants.loc("set_entity_x"), HordeSpawnEntityEvent.class, SetEntityXFunction::deserialize);
		registerFunctionDeserializer(Constants.loc("set_entity_y"), HordeSpawnEntityEvent.class, SetEntityYFunction::deserialize);
		registerFunctionDeserializer(Constants.loc("set_entity_z"), HordeSpawnEntityEvent.class, SetEntityZFunction::deserialize);
		registerFunctionDeserializer(Constants.loc("set_entity_loot_table"), HordeSpawnEntityEvent.class, SetEntityLootTableFunction::deserialize);
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
				ResourceLocation loc = ResourceLocation.tryParse(json.get("name").getAsString());
				Function<JsonElement, Condition> deserializer = CONDITIONS.get(loc);
				if (deserializer == null) throw new NullPointerException("condition " + loc + " is not registered");
				return deserializer.apply(json.get("value"));
			} catch (Exception e) {
				HordesLogger.logError("Failed to read condition " + json, e);
			}
		}
		return null;
	}
	
	public static <T extends HordePlayerEvent> Pair<Class<T>, HordeFunction<T>> readFunction(JsonObject json) {
		if (!(json.has("function") && json.has("value"))) return Pair.of(null, null);
		if (json.get("function").getAsString().equals("hordes:multiple")) return MultipleFunction.deserialize(json.get("value").getAsJsonArray());
		try {
			ResourceLocation loc = ResourceLocation.tryParse(json.get("function").getAsString());
			Pair<Class<? extends HordePlayerEvent>, Function<JsonElement, HordeFunction<? extends HordePlayerEvent>>> pair
					= FUNCTIONS.get(loc);
			if (pair == null) throw new NullPointerException("function " + loc + " is not registered");
			return Pair.of((Class<T>) pair.getFirst(), (HordeFunction<T>) pair.getSecond().apply(json.get("value")));
		} catch (Exception e) {
			HordesLogger.logError("Failed to read function " + json, e);
			return Pair.of(null, null);
		}
	}

	public static void registerValueGetter(ResourceLocation name, BiFunction<JsonObject, DataType, ValueGetter> getter) {
		VALUE_GETTERS.put(name, getter);
	}

	public static void registerConditionDeserializer(ResourceLocation name, Function<JsonElement, Condition> serializer) {
		CONDITIONS.put(name, serializer);
	}
	
	public static <T extends HordePlayerEvent> void registerFunctionDeserializer(ResourceLocation name, Class<T> clazz, Function<JsonElement, HordeFunction<T>> serializer) {
		FUNCTIONS.put(name, new Pair(clazz, serializer));
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
