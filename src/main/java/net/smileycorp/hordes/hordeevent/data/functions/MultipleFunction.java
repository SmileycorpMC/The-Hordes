package net.smileycorp.hordes.hordeevent.data.functions;

import com.mojang.datafixers.util.Pair;
import net.smileycorp.hordes.common.data.conditions.Condition;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

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
        for (Condition condition : pair.getFirst()) if (!condition.apply(event.getEntityWorld(), event.getEntity(), event.getRandom())) return;
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
    
}
