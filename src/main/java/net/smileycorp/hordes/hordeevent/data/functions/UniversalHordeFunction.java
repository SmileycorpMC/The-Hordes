package net.smileycorp.hordes.hordeevent.data.functions;

import net.smileycorp.hordes.common.data.conditions.Condition;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

import java.util.List;

public interface UniversalHordeFunction<T extends HordePlayerEvent> extends HordeFunction<T> {

    Class<T> getEventClass();

    default boolean canApply(List<Condition> conditions, T event) {
        for (Condition condition : conditions) if (!condition.apply(event.getEntityWorld(), event.getEntity(), event.getPlayer(), event.getRandom())) return false;
        return true;
    }

}
