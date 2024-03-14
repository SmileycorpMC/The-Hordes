package net.smileycorp.hordes.hordeevent.data.functions;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.functions.spawndata.*;

import java.util.Map;
import java.util.function.Function;

public class FunctionRegistry {

    private static Map<ResourceLocation, Pair<Class<? extends HordePlayerEvent>, Function<JsonElement, HordeFunction<? extends HordePlayerEvent>>>> DESERIALIZERS = Maps.newHashMap();

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
    }

    public static <T extends HordePlayerEvent> Pair<Class<T>, HordeFunction<T>> readFunction(JsonObject json) {
        if (!(json.has("function") && json.has("value"))) return Pair.of(null, null);
        if (json.get("function").getAsString().equals("hordes:multiple")) return MultipleFunction.deserialize(json.get("value").getAsJsonArray());
        try {
            ResourceLocation loc = new ResourceLocation(json.get("function").getAsString());
            Pair<Class<? extends HordePlayerEvent>, Function<JsonElement, HordeFunction<? extends HordePlayerEvent>>> pair
                    = DESERIALIZERS.get(loc);
            if (pair == null) throw new NullPointerException("function " + loc + " is not registered");
            return Pair.of((Class<T>) pair.getFirst(), (HordeFunction<T>) pair.getSecond().apply(json.get("value")));
        } catch (Exception e) {
            HordesLogger.logError("Failed to read function " + json, e);
            return Pair.of(null, null);
        }
    }

    public static <T extends HordePlayerEvent> void registerFunctionDeserializer(ResourceLocation name, Class<T> clazz, Function<JsonElement, HordeFunction<T>> serializer) {
        DESERIALIZERS.put(name, new Pair(clazz, serializer));
    }

}
