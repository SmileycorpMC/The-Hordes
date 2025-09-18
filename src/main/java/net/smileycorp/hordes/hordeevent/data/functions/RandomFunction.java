package net.smileycorp.hordes.hordeevent.data.functions;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.common.data.conditions.Condition;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

import java.util.List;

public class RandomFunction<T extends HordePlayerEvent> implements UniversalHordeFunction<T> {

    private final Class<T> clazz;
    private final List<Pair<List<Condition>, HordeFunction<T>>> functions;

    public RandomFunction(Class<T> clazz, List<Pair<List<Condition>, HordeFunction<T>>> functions) {
        this.clazz = clazz;
        this.functions = functions;
    }
    
    @Override
    public void apply(T event) {
        if (event.getClass() != clazz) return;
        List<HordeFunction<T>> functions = this.functions.stream().filter(pair -> canApply(pair.getFirst(), event)).map(Pair::getSecond).toList();
        if (functions.isEmpty()) return;
        functions.get(event.getRandom().nextInt(functions.size())).apply(event);
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
    
    public static <T extends HordePlayerEvent> UniversalHordeFunction<T> deserialize(JsonElement json) {
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
            else if (clazz == pair.getFirst()) {
                List<Condition> conditions = Lists.newArrayList();
                if (obj.has("conditions")) obj.get("conditions").getAsJsonArray().forEach(condition ->
                        conditions.add(DataRegistry.readCondition(condition.getAsJsonObject())));
                functions.add(Pair.of(conditions, pair.getSecond()));
            }
        }
        return new RandomFunction(clazz, functions);
    }

}
