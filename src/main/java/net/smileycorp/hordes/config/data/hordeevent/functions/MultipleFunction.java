package net.smileycorp.hordes.config.data.hordeevent.functions;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.DataRegistry;
import net.smileycorp.hordes.config.data.conditions.Condition;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class MultipleFunction<T extends HordePlayerEvent> implements HordeFunction<T> {
    
    private final Class<T> clazz;
    private final List<Map.Entry<List<Condition>, HordeFunction<T>>> functions;
    
    public MultipleFunction(Class<T> clazz, List<Map.Entry<List<Condition>, HordeFunction<T>>> functions) {
        this.clazz = clazz;
        this.functions = functions;
    }
    
    @Override
    public void apply(T event) {
        if (event.getClass() != clazz) return;
        functions.forEach(pair -> tryApply(pair, event));
    }
    
    private void tryApply(Map.Entry<List<Condition>, HordeFunction<T>> pair, T event) {
        for (Condition condition : pair.getKey()) if (!condition.apply(event.getEntityWorld(), event.getEntity(), event.getPlayer(), event.getRandom())) return;
        pair.getValue().apply(event);
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
    
    public static <T extends HordePlayerEvent> Map.Entry<Class<T>, HordeFunction<T>> deserialize(JsonArray json) {
        Class<T> clazz = null;
        List<Map.Entry<List<Condition>, HordeFunction<T>>> functions = Lists.newArrayList();
        for (JsonElement element : json) {
            JsonObject obj = element.getAsJsonObject();
            Map.Entry<Class<T>, HordeFunction<T>> pair = FunctionRegistry.readFunction(obj);
            if (clazz == null && pair.getKey() != null) {
                List<Condition> conditions = Lists.newArrayList();
                if (obj.has("conditions")) obj.get("conditions").getAsJsonArray().forEach(condition ->
                        conditions.add(DataRegistry.readCondition(condition.getAsJsonObject())));
                clazz = pair.getKey();
                functions.add(new AbstractMap.SimpleEntry<>(conditions, pair.getValue()));
            }
            else if (clazz == pair.getKey()) {
                List<Condition> conditions = Lists.newArrayList();
                if (obj.has("conditions")) obj.get("conditions").getAsJsonArray().forEach(condition ->
                        conditions.add(DataRegistry.readCondition(condition.getAsJsonObject())));
                functions.add(new AbstractMap.SimpleEntry<>(conditions, pair.getValue()));
            }
        }
        return new AbstractMap.SimpleEntry<>(clazz, new MultipleFunction(clazz, functions));
    }
    
}
