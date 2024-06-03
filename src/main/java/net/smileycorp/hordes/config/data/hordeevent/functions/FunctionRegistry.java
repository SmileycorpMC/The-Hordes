package net.smileycorp.hordes.config.data.hordeevent.functions;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.common.event.HordeSpawnEntityEvent;
import net.smileycorp.hordes.config.data.hordeevent.functions.spawndata.*;
import net.smileycorp.hordes.config.data.hordeevent.functions.spawnentity.*;

import java.util.AbstractMap;
import java.util.Map;

public class FunctionRegistry {

    private static Map<ResourceLocation, Map.Entry<Class<? extends HordePlayerEvent>, HordeFunction.Deserializer<? extends HordePlayerEvent>>> DESERIALIZERS = Maps.newHashMap();

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
        //spawn entity functions
        registerFunctionDeserializer(Constants.loc("set_entity_type"), HordeSpawnEntityEvent.class, SetEntityTypeFunction::deserialize);
        registerFunctionDeserializer(Constants.loc("set_entity_nbt"), HordeSpawnEntityEvent.class, SetEntityNBTFunction::deserialize);
        registerFunctionDeserializer(Constants.loc("set_entity_x"), HordeSpawnEntityEvent.class, SetEntityXFunction::deserialize);
        registerFunctionDeserializer(Constants.loc("set_entity_y"), HordeSpawnEntityEvent.class, SetEntityYFunction::deserialize);
        registerFunctionDeserializer(Constants.loc("set_entity_z"), HordeSpawnEntityEvent.class, SetEntityZFunction::deserialize);
        registerFunctionDeserializer(Constants.loc("set_entity_loot_table"), HordeSpawnEntityEvent.class, SetEntityLootTableFunction::deserialize);
    }

    public static <T extends HordePlayerEvent> Map.Entry<Class<T>, HordeFunction<T>> readFunction(JsonObject json) {
        if (!(json.has("function") && json.has("value"))) return new AbstractMap.SimpleEntry<>(null, null);
        if (json.get("function").getAsString().equals("hordes:multiple")) return MultipleFunction.deserialize(json.get("value").getAsJsonArray());
        try {
            ResourceLocation loc = new ResourceLocation(json.get("function").getAsString());
            Map.Entry<Class<? extends HordePlayerEvent>, HordeFunction.Deserializer<? extends HordePlayerEvent>> pair = DESERIALIZERS.get(loc);
            if (pair == null) throw new NullPointerException("function " + loc + " is not registered");
            return new AbstractMap.SimpleEntry<>((Class<T>) pair.getKey(), (HordeFunction<T>) pair.getValue().apply(json.get("value")));
        } catch (Exception e) {
            HordesLogger.logError("Failed to read function " + json, e);
            return new AbstractMap.SimpleEntry<>(null, null);
        }
    }

    public static <T extends HordePlayerEvent> void registerFunctionDeserializer(ResourceLocation name, Class<T> clazz, HordeFunction.Deserializer<T> serializer) {
        DESERIALIZERS.put(name, new AbstractMap.SimpleEntry<>(clazz, serializer));
    }

}
