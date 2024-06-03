package net.smileycorp.hordes.config.data.hordeevent.functions.spawndata;

import com.google.gson.JsonElement;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.config.data.DataType;
import net.smileycorp.hordes.config.data.hordeevent.functions.HordeFunction;
import net.smileycorp.hordes.config.data.values.ValueGetter;

public class SetSpawnIntervalFunction implements HordeFunction<HordeBuildSpawnDataEvent> {

    private final ValueGetter<Integer> getter;

    public SetSpawnIntervalFunction(ValueGetter<Integer> getter) {
        this.getter = getter;
    }

    @Override
    public void apply(HordeBuildSpawnDataEvent event) {
        event.getSpawnData().setSpawnInterval(getter.get(event));
    }

    public static SetSpawnIntervalFunction deserialize(JsonElement json) {
        try {
            return new SetSpawnIntervalFunction(ValueGetter.readValue(DataType.INT, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_spawn_interval", e);
        }
        return null;
    }
    
}
