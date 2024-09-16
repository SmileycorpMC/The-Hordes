package net.smileycorp.hordes.hordeevent.data.functions.spawndata;

import com.google.gson.JsonElement;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;
import net.smileycorp.hordes.hordeevent.data.values.ValueGetter;

public class AddRewardCommandFunction implements HordeFunction<HordeBuildSpawnDataEvent> {
    
    private final ValueGetter<String> getter;
    
    public AddRewardCommandFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeBuildSpawnDataEvent event) {
        event.getSpawnData().addCommand(getter.get(event));
    }
    
    public static AddRewardCommandFunction deserialize(JsonElement json) {
        try {
            return new AddRewardCommandFunction(ValueGetter.readValue(DataType.STRING, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_end_message", e);
        }
        return null;
    }
    
}
