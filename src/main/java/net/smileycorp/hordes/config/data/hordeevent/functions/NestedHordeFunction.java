package net.smileycorp.hordes.config.data.hordeevent.functions;

import com.google.gson.JsonElement;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.conditions.Condition;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;

import java.util.List;

public interface NestedHordeFunction<T extends HordePlayerEvent> extends HordeFunction<T> {

    Class<T> getEventClass();

    default boolean canApply(List<Condition> conditions, HordeContext<T> ctx) {
        for (Condition condition : conditions) if (!condition.apply(ctx)) return false;
        return true;
    }

    interface Deserializer<T extends HordePlayerEvent> extends HordeFunction.Deserializer<T> {

        NestedHordeFunction<T> apply(JsonElement element);

    }

}
