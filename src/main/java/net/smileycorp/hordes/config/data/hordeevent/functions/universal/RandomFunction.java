package net.smileycorp.hordes.config.data.hordeevent.functions.universal;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.smileycorp.atlas.api.data.Pair;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.DataRegistry;
import net.smileycorp.hordes.config.data.conditions.Condition;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;
import net.smileycorp.hordes.config.data.hordeevent.functions.FunctionRegistry;
import net.smileycorp.hordes.config.data.hordeevent.functions.HordeFunction;
import net.smileycorp.hordes.config.data.hordeevent.functions.NestedHordeFunction;

import java.util.List;
import java.util.stream.Collectors;

public class RandomFunction<T extends HordePlayerEvent> implements NestedHordeFunction<T> {

    private final Class<T> clazz;
    private final List<Pair<List<Condition>, HordeFunction<T>>> functions;

    public RandomFunction(Class<T> clazz, List<Pair<List<Condition>, HordeFunction<T>>> functions) {
        this.clazz = clazz;
        this.functions = functions;
    }
    
    @Override
    public void apply(HordeContext<T> ctx) {
        if (ctx.getEventClass() != clazz) return;
        List<HordeFunction<T>> functions = this.functions.stream().filter(pair -> canApply(pair.getFirst(), ctx))
                .map(Pair::getSecond).collect(Collectors.toList());
        if (functions.isEmpty()) return;
        functions.get(ctx.getRandom().nextInt(functions.size())).apply(ctx);
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
    
    public static <T extends HordePlayerEvent> RandomFunction<T> deserialize(JsonElement json) {
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
            return new RandomFunction(clazz, functions);
        } catch (Exception e) {
            HordesLogger.logError("Error reading hordes:random function", e);
            return null;
        }
    }

}
