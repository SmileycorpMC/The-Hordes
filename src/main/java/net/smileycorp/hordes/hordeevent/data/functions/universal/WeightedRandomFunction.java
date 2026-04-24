package net.smileycorp.hordes.hordeevent.data.functions.universal;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.smileycorp.atlas.api.util.WeightedOutputs;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.common.data.conditions.Condition;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;
import net.smileycorp.hordes.hordeevent.data.functions.FunctionRegistry;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;
import net.smileycorp.hordes.hordeevent.data.functions.NestedHordeFunction;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class WeightedRandomFunction<T extends HordePlayerEvent> implements NestedHordeFunction<T> {

    private final Class<T> clazz;
    private final List<Pair<List<Condition>, Pair<Integer, HordeFunction<T>>>> functions;

    public WeightedRandomFunction(Class<T> clazz, List<Pair<List<Condition>, Pair<Integer, HordeFunction<T>>>> functions) {
        this.clazz = clazz;
        this.functions = functions;
    }
    
    @Override
    public void apply(HordeContext<T> ctx) {
        if (ctx.getEventClass() != clazz) return;
        WeightedOutputs<HordeFunction<T>> functions = new WeightedOutputs(1, this.functions.stream().
                filter(pair -> canApply(pair.getFirst(), ctx))
                .map(WeightedRandomFunction::mapEntry).toList());
        if (functions.isEmpty()) return;
        functions.getResults(ctx.getRandom()).forEach(func -> apply(ctx));
    }

    private static <T extends HordePlayerEvent> Map.Entry<Integer, HordeFunction<T>> mapEntry(Pair<List<Condition>, Pair<Integer, HordeFunction<T>>> pair) {
        Pair<Integer, HordeFunction<T>> subpair = pair.getSecond();
        return new AbstractMap.SimpleEntry<>(subpair.getFirst(), subpair.getSecond());
    }

    @Override
    public Class<T> getEventClass() {
        return clazz;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < functions.size(); i++) {
            builder.append(functions.get(i).toString());
            if (i < functions.size() - 1) builder.append(" && ");
        }
        return super.toString() + "[" + builder + "]";
    }
    
    public static <T extends HordePlayerEvent> WeightedRandomFunction<T> deserialize(JsonElement json) {
        try {
            Class<T> clazz = null;
            List<Pair<List<Condition>, HordeFunction<T>>> functions = Lists.newArrayList();
            for (JsonElement element : json.getAsJsonArray()) {
                JsonObject obj = element.getAsJsonObject();
                Pair<Class<T>, HordeFunction<T>> pair = FunctionRegistry.readFunction(obj);
                if (clazz == null && pair.getFirst() != null) {
                    List<Condition> conditions = Lists.newArrayList();
                    if (obj.has("conditions")) obj.get("conditions").getAsJsonArray().forEach(condition ->
                            conditions.add(DataRegistry.readCondition(condition.getAsJsonObject())));
                    clazz = pair.getFirst();
                    functions.add(Pair.of(conditions, pair.getSecond()));
                }
                else if (pair.getFirst() != null && clazz != null && pair.getFirst().isAssignableFrom(clazz)) {
                    List<Condition> conditions = Lists.newArrayList();
                    if (obj.has("conditions")) obj.get("conditions").getAsJsonArray().forEach(condition ->
                            conditions.add(DataRegistry.readCondition(condition.getAsJsonObject())));
                    functions.add(Pair.of(conditions, pair.getSecond()));
                }
            }
            return new WeightedRandomFunction(clazz, functions);
        } catch (Exception e) {
            HordesLogger.logError("Error reading hordes:weighted_random function", e);
            return null;
        }
    }

}
