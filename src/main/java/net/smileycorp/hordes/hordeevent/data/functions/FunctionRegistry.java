package net.smileycorp.hordes.hordeevent.data.functions;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FunctionRegistry {

    private static Map<ResourceLocation, Pair<Class<? extends HordePlayerEvent>, Function<JsonElement, HordeFunction<? extends HordePlayerEvent>>>> DESERIALIZERS = Maps.newHashMap();

    public static void registerFunctionSerializers() {
        registerFunctionDeserializer(Constants.loc("set_spawntable"), HordeBuildSpawnDataEvent.class, SetSpawntableFunction::deserialize);
        registerFunctionDeserializer(Constants.loc("set_spawn_type"), HordeBuildSpawnDataEvent.class, SetSpawnTypeFunction::deserialize);
        registerFunctionDeserializer(Constants.loc("set_spawn_sound"), HordeBuildSpawnDataEvent.class, SetSpawnSoundFunction::deserialize);
        registerFunctionDeserializer(Constants.loc("set_start_message"), HordeBuildSpawnDataEvent.class, SetStartMessageFunction::deserialize);
        registerFunctionDeserializer(Constants.loc("set_end_message"), HordeBuildSpawnDataEvent.class, SetEndMessageFunction::deserialize);
    }

    public static Pair<Class<? extends HordePlayerEvent>, HordeFunction<? extends HordePlayerEvent>> readFunction(JsonObject json) {
        if (json.has("function") && json.has("value")) {
            if (json.get("function").equals("hordes:multiple")) {
                Class<? extends HordePlayerEvent> clazz = null;
                List<HordeFunction<?>> functions = Lists.newArrayList();
                for (JsonElement element : json.get("value").getAsJsonArray()) {
                    Pair<Class<? extends HordePlayerEvent>, HordeFunction<? extends HordePlayerEvent>> pair = readFunction(element.getAsJsonObject());
                    if (clazz == null && pair.getFirst() != null) {
                        clazz = pair.getFirst();
                        functions.add(pair.getSecond());
                    }
                    else if (clazz == pair.getFirst());
                }
                return Pair.of(clazz, new MultipleFunction(clazz, functions));
            }
            try {
                Pair<Class<? extends HordePlayerEvent>, Function<JsonElement, HordeFunction<? extends HordePlayerEvent>>> pair
                        = DESERIALIZERS.get(new ResourceLocation(json.get("function").getAsString()));
                return pair.mapSecond(serializer -> serializer.apply(json.get("value")));
            } catch (Exception e) {
                HordesLogger.logError("Failed to read condition " + json, e);
            }
        }
        return Pair.of(null, null);
    }

    public static <T extends HordePlayerEvent> void registerFunctionDeserializer(ResourceLocation name, Class<T> clazz, Function<JsonElement, HordeFunction<T>> serializer) {
        DESERIALIZERS.put(name, new Pair(clazz, serializer));
    }

}
