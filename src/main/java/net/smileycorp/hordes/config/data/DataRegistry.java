package net.smileycorp.hordes.config.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.smileycorp.atlas.api.data.BinaryOperation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.LogicalOperation;
import net.smileycorp.atlas.api.data.UnaryOperation;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.config.data.conditions.*;
import net.smileycorp.hordes.config.data.hordeevent.functions.FunctionRegistry;
import net.smileycorp.hordes.config.data.values.*;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DataRegistry {

	private static final Map<ResourceLocation, BiFunction<JsonObject, DataType, ValueGetter>> VALUE_GETTERS = Maps.newHashMap();
	private static final Map<ResourceLocation, Function<JsonElement, Condition>> CONDITION_DESERIALIZERS = Maps.newHashMap();

	public static void init() {
		registerValueGetters();
		registerConditionDeserializers();
		if (HordeEventConfig.enableHordeEvent) FunctionRegistry.registerFunctionSerializers();
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
		registerValueGetter(Constants.loc("get_variable"), VariableGetter::deserialize);
	}

	public static void registerConditionDeserializers() {
		for (LogicalOperation operation : LogicalOperation.values())
			registerConditionDeserializer(Constants.loc(operation.getName()), e -> LogicalCondition.deserialize(operation, e));
		registerConditionDeserializer(Constants.loc("not"), NotCondition::deserialize);
		registerConditionDeserializer(Constants.loc("comparison"), ComparisonCondition::deserialize);
		registerConditionDeserializer(Constants.loc("biome"), BiomeCondition::deserialize);
		registerConditionDeserializer(Constants.loc("day"), DayCondition::deserialize);
		registerConditionDeserializer(Constants.loc("player_day"), DayCondition::deserialize);
		registerConditionDeserializer(Constants.loc("local_difficulty"), LocalDifficultyCondition::deserialize);
		registerConditionDeserializer(Constants.loc("game_difficulty"), GameDifficultyCondition::deserialize);
		registerConditionDeserializer(Constants.loc("random"), RandomCondition::deserialize);
		registerConditionDeserializer(Constants.loc("advancement"), AdvancementCondition::deserialize);
		registerConditionDeserializer(Constants.loc("entity_type"), EntityTypeCondition::deserialize);
		registerConditionDeserializer(Constants.loc("is_called"), IsCalledCondition::deserialize);
		if (Loader.isModLoaded("gamestages")) registerConditionDeserializer(
				new ResourceLocation("gamestages:gamestage"), GameStagesCondition::deserialize);
	}

	public static ValueGetter readValue(DataType type, JsonObject json) {
		try {
			ResourceLocation loc = new ResourceLocation(json.get("name").getAsString());
			BiFunction<JsonObject, DataType, ValueGetter> getter = VALUE_GETTERS.get(loc);
			if (getter == null) throw new NullPointerException("value getter " + loc + " is not registered");
			return getter.apply(json, type);
		} catch (Exception e) {
			HordesLogger.logError("Failed to read value " + json, e);
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

    public static NBTTagCompound parseNBT(String name, String nbtstring) {
		NBTTagCompound nbt = null;
        try {
			NBTTagCompound parsed = JsonToNBT.getTagFromJson(nbtstring);
            if (parsed != null) nbt = parsed;
            else throw new NullPointerException("Parsed NBT is null.");
        } catch (Exception e) {
            HordesLogger.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
            HordesLogger.logError("Error parsing nbt for entity " + name + " " + e.getMessage(), e);
        }
        return nbt;
    }
	
}
