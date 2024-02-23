package net.smileycorp.hordes.hordeevent.data.functions;

import com.google.gson.JsonElement;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;

public class SetEndMessageFunction implements HordeFunction<HordeBuildSpawnDataEvent> {

    private final ValueGetter<String> getter;

    public SetEndMessageFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }

    @Override
    public void apply(HordeBuildSpawnDataEvent event) {
        event.getSpawnData().setEndMessage(getter.get(event.getEntityWorld(), event.getEntity(), event.getRandom()));
    }

    public static SetEndMessageFunction deserialize(JsonElement json) {
        try {
            return new SetEndMessageFunction(ValueGetter.readValue(DataType.STRING, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_end_message", e);
        }
        return null;
    }
}
