package net.smileycorp.hordes.hordeevent.data.functions;

import net.smileycorp.hordes.common.data.conditions.Condition;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

import java.util.List;

public interface NestedHordeFunction<T extends HordePlayerEvent> extends HordeFunction<T> {

    Class<T> getEventClass();

    default boolean canApply(List<Condition> conditions, HordeContext<T> ctx) {
        for (Condition condition : conditions) if (!condition.apply(ctx)) return false;
        return true;
    }

}
