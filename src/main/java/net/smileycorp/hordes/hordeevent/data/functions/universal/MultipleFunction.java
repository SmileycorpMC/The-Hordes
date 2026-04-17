package net.smileycorp.hordes.hordeevent.data.functions.universal;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.common.data.conditions.Condition;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;
import net.smileycorp.hordes.hordeevent.data.functions.FunctionRegistry;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;
import net.smileycorp.hordes.hordeevent.data.functions.NestedHordeFunction;

import java.util.List;

public class MultipleFunction<T extends HordePlayerEvent> implements NestedHordeFunction<T> {
    
    private final Class<T> clazz;
    private final List<Pair<List<Condition>, HordeFunction<T>>> functions;
    
    public MultipleFunction(Class<T> clazz, List<Pair<List<Condition>, HordeFunction<T>>> functions) {
        this.clazz = clazz;
        this.functions = functions;
    }
    
    @Override
    public void apply(HordeContext<T> ctx) {
        if (ctx.getEventClass() != clazz) return;
        for (Pair<List<Condition>, HordeFunction<T>> pair : functions) {
            if (canApply(pair.getFirst(), ctx)) pair.getSecond().apply(ctx);
            if (ctx.isBroken()) break;
        }
        ctx.resetState();
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
    
    public static <T extends HordePlayerEvent> MultipleFunction<T> deserialize(JsonElement json) {
        try {
            Class<T> clazz = null;
            List<Pair<List<Condition>, HordeFunction<T>>> functions = Lists.newArrayList();
            for (JsonElement element : json.getAsJsonArray()) {
                JsonObject obj = element.getAsJsonObject();
                Pair<Class<T>, HordeFunction<T>> pair = FunctionRegistry.readFunction(obj);
                Class<T> clazz1 = pair.getFirst();
                if (clazz == null && clazz1 != null) {
                    List<Condition> conditions = Lists.newArrayList();
                    if (obj.has("conditions")) obj.get("conditions").getAsJsonArray().forEach(condition ->
                            conditions.add(DataRegistry.readCondition(condition.getAsJsonObject())));
                    clazz = clazz1;
                    functions.add(Pair.of(conditions, pair.getSecond()));
                }
                else if (clazz != null && clazz1 != null && (clazz1.isAssignableFrom(clazz) || clazz.isAssignableFrom(clazz1))) {
                    List<Condition> conditions = Lists.newArrayList();
                    if (obj.has("conditions")) obj.get("conditions").getAsJsonArray().forEach(condition ->
                            conditions.add(DataRegistry.readCondition(condition.getAsJsonObject())));
                    functions.add(Pair.of(conditions, pair.getSecond()));
                }
            }
            return new MultipleFunction(clazz, functions);
        } catch (Exception e) {
            HordesLogger.logError("Error reading hordes:multiple function", e);
            return null;
        }
    }

}
