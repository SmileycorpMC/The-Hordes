package net.smileycorp.hordes.hordeevent.data.functions;

import net.smileycorp.hordes.common.data.conditions.Condition;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

import java.util.List;

public interface UniversalHordeFunction<T extends HordePlayerEvent> extends HordeFunction<T> {

    Class<T> getEventClass();

    default boolean canApply(List<Condition> conditions, HordeContext<T> ctx) {
        for (Condition condition : conditions) if (!condition.apply(ctx.getEntityWorld(), ctx.getEntity(), ctx.getPlayer(), ctx.getRandom())) return false;
        return true;
    }

}
