package net.smileycorp.hordes.hordeevent.data.functions;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.conditions.Condition;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordeBuildSpawntableEvent;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.common.event.HordeStartEvent;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class FunctionRegistry {

    private static Map<ResourceLocation, Pair<Class<? extends HordePlayerEvent>, Function<JsonElement, HordeFunction<? extends HordePlayerEvent>>>> DESERIALIZERS = Maps.newHashMap();

    public static void registerFunctionSerializers() {
        registerFunctionDeserializer(Constants.loc("set_spawntable"), HordeBuildSpawntableEvent.class, SetSpawntableFunction::deserialize);
    }

    public static Pair<Class<? extends HordePlayerEvent>, HordeFunction<? extends HordePlayerEvent>> readFunction(JsonObject json) {
        if (json.has("function") && json.has("value")) {
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
