package net.smileycorp.hordes.hordeevent.data.functions.spawndata;

import com.google.gson.JsonElement;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;

public class AddRewardCommandFunction implements HordeFunction<HordeBuildSpawnDataEvent> {
    
    private final ValueGetter<String> getter;
    
    public AddRewardCommandFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeContext<HordeBuildSpawnDataEvent> ctx) {
        ctx.getSpawnData().addCommand(getter.get(ctx));
    }
    
    public static AddRewardCommandFunction deserialize(JsonElement json) {
        try {
            return new AddRewardCommandFunction(ValueGetter.readValue(DataType.STRING, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:add_reward_command", e);
        }
        return null;
    }
    
}
