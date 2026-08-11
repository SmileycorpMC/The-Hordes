package net.smileycorp.hordes.hordeevent.data.functions.universal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;

public class SetGlobalFunction implements HordeFunction<HordePlayerEvent> {

    private final ValueGetter<String> variable;
    private final ValueGetter<?> value;

    public SetGlobalFunction(ValueGetter<String> variable, ValueGetter<?> value) {
        this.variable =  variable;
        this.value = value;
    }
    
    @Override
    public void apply(HordeContext<HordePlayerEvent> ctx) {
        ctx.setGlobal(variable.get(ctx), value.get(ctx));
    }

    public static SetGlobalFunction deserialize(JsonElement json) {
        try {
            JsonObject obj = (JsonObject) json;
            DataType type = DataType.of(obj.get("type").getAsString());
            return new SetGlobalFunction(ValueGetter.readValue(DataType.STRING, obj.get("key")), ValueGetter.readValue(type, obj.get("value")));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_global", e);
        }
        return null;
    }

}
