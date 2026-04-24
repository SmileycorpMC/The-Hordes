package net.smileycorp.hordes.hordeevent.data.functions;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.data.HordesParsingException;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.common.event.HordeSpawnEntityEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;
import net.smileycorp.hordes.hordeevent.data.functions.spawndata.*;
import net.smileycorp.hordes.hordeevent.data.functions.spawnentity.*;
import net.smileycorp.hordes.hordeevent.data.functions.universal.*;

import java.util.Map;
import java.util.function.Function;

public class FunctionRegistry {

    private static Map<ResourceLocation, Pair<Class<? extends HordePlayerEvent>, Function<JsonElement, HordeFunction<? extends HordePlayerEvent>>>> DESERIALIZERS = Maps.newHashMap();

    public static void registerFunctionSerializers() {
        //universal functions
        registerNestedFunction(Constants.loc("multiple"), MultipleFunction::deserialize);
        registerNestedFunction(Constants.loc("random"), RandomFunction::deserialize);
        registerNestedFunction(Constants.loc("weighted_random"), WeightedRandomFunction::deserialize);
        registerFunction(Constants.loc("set_entity_nbt"), HordePlayerEvent.class, SetEntityNBTFunction::deserialize);
        registerFunction(Constants.loc("set_player_nbt"), HordePlayerEvent.class, SetPlayerNBTFunction::deserialize);
        registerFunction(Constants.loc("set_variable"), HordePlayerEvent.class, SetVariableFunction::deserialize);
        registerFunction(Constants.loc("call_script"), HordePlayerEvent.class, CallScriptFunction::deserialize);
        registerInstructionFunction(Constants.loc("break"), HordeContext::breakScript);
        registerInstructionFunction(Constants.loc("return"), HordeContext::returnScript);
        registerInstructionFunction(Constants.loc("cancel"), HordeContext::cancelEvent);

        //build spawndata functions
        registerFunction(Constants.loc("set_spawntable"), HordeBuildSpawnDataEvent.class, SetSpawntableFunction::deserialize);
        registerFunction(Constants.loc("set_spawn_type"), HordeBuildSpawnDataEvent.class, SetSpawnTypeFunction::deserialize);
        registerFunction(Constants.loc("set_spawn_sound"), HordeBuildSpawnDataEvent.class, SetSpawnSoundFunction::deserialize);
        registerFunction(Constants.loc("set_start_message"), HordeBuildSpawnDataEvent.class, SetStartMessageFunction::deserialize);
        registerFunction(Constants.loc("set_end_message"), HordeBuildSpawnDataEvent.class, SetEndMessageFunction::deserialize);
        registerFunction(Constants.loc("set_spawn_duration"), HordeBuildSpawnDataEvent.class, SetSpawnDurationFunction::deserialize);
        registerFunction(Constants.loc("set_spawn_interval"), HordeBuildSpawnDataEvent.class, SetSpawnIntervalFunction::deserialize);
        registerFunction(Constants.loc("set_spawn_amount"), HordeBuildSpawnDataEvent.class, SetSpawnAmountFunction::deserialize);
        registerFunction(Constants.loc("set_entity_speed"), HordeBuildSpawnDataEvent.class, SetEntitySpeedFunction::deserialize);
        registerFunction(Constants.loc("add_reward_command"), HordeBuildSpawnDataEvent.class, AddRewardCommandFunction::deserialize);
        //spawn entity functions
        registerFunction(Constants.loc("set_entity_type"), HordeSpawnEntityEvent.class, SetEntityTypeFunction::deserialize);
        registerFunction(Constants.loc("set_entity_x"), HordeSpawnEntityEvent.class, SetEntityXFunction::deserialize);
        registerFunction(Constants.loc("set_entity_y"), HordeSpawnEntityEvent.class, SetEntityYFunction::deserialize);
        registerFunction(Constants.loc("set_entity_z"), HordeSpawnEntityEvent.class, SetEntityZFunction::deserialize);
        registerFunction(Constants.loc("set_entity_loot_table"), HordeSpawnEntityEvent.class, SetEntityLootTableFunction::deserialize);
    }

    public static <T extends HordePlayerEvent> Pair<Class<T>, HordeFunction<T>> readFunction(JsonObject json) throws Exception {
        if (!(json.has("function"))) return Pair.of(null, null);
        ResourceLocation loc = new ResourceLocation(json.get("function").getAsString());
        Pair<Class<? extends HordePlayerEvent>, Function<JsonElement, HordeFunction<? extends HordePlayerEvent>>> pair
                = DESERIALIZERS.get(loc);
        if (pair == null) throw new HordesParsingException("function " + loc + " is not registered");
        HordeFunction<T> function = (HordeFunction<T>) pair.getSecond().apply(json.has("value") ?
                json.get("value") : new JsonNull());
        return Pair.of(function instanceof NestedHordeFunction ? ((NestedHordeFunction<T>) function).getEventClass()
                : (Class<T>) pair.getFirst(), function);
    }

    public static void registerNestedFunction(ResourceLocation name, Function<JsonElement, NestedHordeFunction> serializer) {
        DESERIALIZERS.put(name, new Pair(null, serializer));
    }

    public static <T extends HordePlayerEvent> void registerInstructionFunction(ResourceLocation name, HordeFunction<HordePlayerEvent> function) {
        DESERIALIZERS.put(name, new Pair<>(HordePlayerEvent.class, json -> function));
    }

    public static <T extends HordePlayerEvent> void registerFunction(ResourceLocation name, Class<T> clazz, Function<JsonElement, HordeFunction<T>> serializer) {
        if (clazz == null) return;
        DESERIALIZERS.put(name, new Pair(clazz, serializer));
    }

}
