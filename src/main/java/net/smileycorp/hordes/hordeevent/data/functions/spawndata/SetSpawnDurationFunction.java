package net.smileycorp.hordes.hordeevent.data.functions.spawndata;

import com.google.gson.JsonElement;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;

public class SetSpawnDurationFunction implements HordeFunction<HordeBuildSpawnDataEvent> {

    private final ValueGetter<Integer> getter;

    public SetSpawnDurationFunction(ValueGetter<Integer> getter) {
        this.getter = getter;
    }

    @Override
    public void apply(HordeBuildSpawnDataEvent event) {
        event.getSpawnData().setSpawnDuration(getter.get(event.getEntityWorld(), event.getEntity(), event.getRandom()));
    }

    public static SetSpawnDurationFunction deserialize(JsonElement json) {
        try {
            return new SetSpawnDurationFunction(ValueGetter.readValue(DataType.INT, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_spawn_duration", e);
        }
        return null;
    }
}
