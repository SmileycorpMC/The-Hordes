package net.smileycorp.hordes.hordeevent.data.functions.spawndata;

import com.google.gson.JsonElement;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;

public class SetSpawnSoundFunction implements HordeFunction<HordeBuildSpawnDataEvent> {

    private final ValueGetter<String> getter;

    public SetSpawnSoundFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }

    @Override
    public void apply(HordeBuildSpawnDataEvent event) {
        event.getSpawnData().setSpawnSound(new ResourceLocation(getter.get(event)));
    }

    public static SetSpawnSoundFunction deserialize(JsonElement json) {
        try {
            return new SetSpawnSoundFunction(ValueGetter.readValue(DataType.STRING, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_spawn_sound", e);
        }
        return null;
    }
    
}
