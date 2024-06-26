package net.smileycorp.hordes.hordeevent.data.functions.spawndata;

import com.google.gson.JsonElement;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;
import net.smileycorp.hordes.hordeevent.data.values.ValueGetter;

public class SetSpawnAmountFunction implements HordeFunction<HordeBuildSpawnDataEvent> {

    private final ValueGetter<Integer> getter;

    public SetSpawnAmountFunction(ValueGetter<Integer> getter) {
        this.getter = getter;
    }

    @Override
    public void apply(HordeBuildSpawnDataEvent event) {
        event.getSpawnData().setSpawnAmount(getter.get(event));
    }

    public static SetSpawnAmountFunction deserialize(JsonElement json) {
        try {
            return new SetSpawnAmountFunction(ValueGetter.readValue(DataType.INT, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_spawn_amount", e);
        }
        return null;
    }
    
}
