package net.smileycorp.hordes.config.data.hordeevent.functions.spawndata;

import com.google.gson.JsonElement;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.config.data.DataType;
import net.smileycorp.hordes.config.data.hordeevent.functions.HordeFunction;
import net.smileycorp.hordes.config.data.values.ValueGetter;

public class SetEndMessageFunction implements HordeFunction<HordeBuildSpawnDataEvent> {

    private final ValueGetter<String> getter;

    public SetEndMessageFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }

    @Override
    public void apply(HordeBuildSpawnDataEvent event) {
        event.getSpawnData().setEndMessage(getter.get(event));
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
