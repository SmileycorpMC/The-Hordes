package net.smileycorp.hordes.hordeevent.data.functions;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.DataRegistry;
import net.smileycorp.hordes.hordeevent.data.conditions.Condition;

import java.util.List;

public class MultipleFunction<T extends HordePlayerEvent> implements HordeFunction<T> {
    
    private final Class<T> clazz;
    private final List<Pair<List<Condition>, HordeFunction<T>>> functions;
    
    public MultipleFunction(Class<T> clazz, List<Pair<List<Condition>, HordeFunction<T>>> functions) {
        this.clazz = clazz;
        this.functions = functions;
    }
    
    @Override
    public void apply(T event) {
        if (event.getClass() != clazz) return;
       functions.forEach(pair -> tryApply(pair, event));
    }
    
    private void tryApply(Pair<List<Condition>, HordeFunction<T>> pair, T event) {
        for (Condition condition : pair.getFirst()) if (!condition.apply(event)) return;
        pair.getSecond().apply(event);
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
    
    public static <T extends HordePlayerEvent> Pair<Class<T>, HordeFunction<T>> deserialize(JsonArray json) {
        Class<T> clazz = null;
        List<Pair<List<Condition>, HordeFunction<T>>> functions = Lists.newArrayList();
        for (JsonElement element : json) {
            JsonObject obj = element.getAsJsonObject();
            Pair<Class<T>, HordeFunction<T>> pair = DataRegistry.readFunction(obj);
            if (clazz == null && pair.getFirst() != null) {
                List<Condition> conditions = Lists.newArrayList();
                if (obj.has("conditions")) obj.get("conditions").getAsJsonArray().forEach(condition ->
                        conditions.add(DataRegistry.readCondition(condition.getAsJsonObject())));
                clazz = pair.getFirst();
                functions.add(Pair.of(conditions, pair.getSecond()));
            }
            else if (clazz == pair.getFirst()) {
                List<Condition> conditions = Lists.newArrayList();
                if (obj.has("conditions")) obj.get("conditions").getAsJsonArray().forEach(condition ->
                        conditions.add(DataRegistry.readCondition(condition.getAsJsonObject())));
                functions.add(Pair.of(conditions, pair.getSecond()));
            }
        }
        return Pair.of(clazz, new MultipleFunction(clazz, functions));
    }
    
}
