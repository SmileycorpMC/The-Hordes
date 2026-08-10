package net.smileycorp.hordes.hordeevent.data.functions.universal;

import com.google.gson.JsonElement;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;

public class AdvanceRandomFunction implements HordeFunction<HordePlayerEvent> {

    private final ValueGetter<Integer> getter;

    public AdvanceRandomFunction(ValueGetter<Integer> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeContext<HordePlayerEvent> ctx) {
        ctx.getRandom().consumeCount(getter.get(ctx));
    }
    
    public static AdvanceRandomFunction deserialize(JsonElement json) {
        try {
            return new AdvanceRandomFunction(ValueGetter.readValue(DataType.INT, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:advance_random", e);
        }
        return null;
    }
    
}
