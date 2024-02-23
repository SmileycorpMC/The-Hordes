package net.smileycorp.hordes.hordeevent.data.functions;

import net.smileycorp.hordes.common.event.HordePlayerEvent;

import java.util.List;

public class MultipleFunction<T extends HordePlayerEvent> implements HordeFunction<T> {
    
    private final Class<T> clazz;
    private final List<HordeFunction<T>> functions;
    
    public MultipleFunction(Class<T> clazz, List<HordeFunction<T>> functions) {
        this.clazz = clazz;
        this.functions = functions;
    }
    
    @Override
    public void apply(T event) {
        if (event.getClass() != clazz) return;
        for (HordeFunction<T> function : functions) function.apply(event);
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
