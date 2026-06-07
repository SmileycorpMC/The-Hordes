package net.smileycorp.hordes.config.data.hordeevent.functions.universal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;
import net.smileycorp.hordes.config.data.hordeevent.functions.HordeFunction;
import net.smileycorp.hordes.config.data.values.ValueGetter;

public class SetVariableFunction implements HordeFunction<HordePlayerEvent> {

    private final ValueGetter<String> variable;
    private final ValueGetter<?> value;

    public SetVariableFunction(ValueGetter<String> variable, ValueGetter<?> value) {
        this.variable =  variable;
        this.value = value;
    }
    
    @Override
    public void apply(HordeContext<HordePlayerEvent> ctx) {
        ctx.setValue(variable.get(ctx), value.get(ctx));
    }

    public static SetVariableFunction deserialize(JsonElement json) {
        try {
            JsonObject obj = (JsonObject) json;
            DataType type = DataType.of(obj.get("type").getAsString());
            return new SetVariableFunction(ValueGetter.readValue(DataType.STRING, obj.get("key")), ValueGetter.readValue(type, obj.get("value")));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_variable", e);
        }
        return null;
    }

}
