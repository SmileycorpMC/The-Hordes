package net.smileycorp.hordes.config.data.hordeevent.functions.spawndata;

import com.google.gson.JsonElement;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.hordeevent.HordeSpawnType;
import net.smileycorp.hordes.hordeevent.HordeSpawnTypes;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;

public class SetSpawnTypeFunction implements HordeFunction<HordeBuildSpawnDataEvent> {

    private final HordeSpawnType type;

    public SetSpawnTypeFunction(HordeSpawnType type) {
        this.type = type;
    }

    @Override
    public void apply(HordeBuildSpawnDataEvent event) {
        event.getSpawnData().setSpawnType(type);
    }

    public static SetSpawnTypeFunction deserialize(JsonElement json) {
        try {
            HordeSpawnType type = HordeSpawnTypes.fromJson(json);
            if (type == null) throw new NullPointerException();
            return new SetSpawnTypeFunction(type);
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_spawn_type", e);
        }
        return null;
    }
    
}
