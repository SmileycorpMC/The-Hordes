package net.smileycorp.hordes.hordeevent.data.functions;

import com.google.gson.JsonElement;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.hordeevent.HordeSpawnType;

import java.util.Locale;

public class SetSpawnTypeFunction implements HordeFunction<HordeBuildSpawnDataEvent> {

    private final ValueGetter<String> getter;

    public SetSpawnTypeFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }

    @Override
    public void apply(HordeBuildSpawnDataEvent event) {
        event.getSpawnData().setSpawnType(HordeSpawnType.valueOf(getter.get(event.getEntityWorld(), event.getEntity(), event.getRandom()).toUpperCase(Locale.US)));
    }

    public static SetSpawnTypeFunction deserialize(JsonElement json) {
        try {
            return new SetSpawnTypeFunction(ValueGetter.readValue(DataType.STRING, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_start_message", e);
        }
        return null;
    }
}
