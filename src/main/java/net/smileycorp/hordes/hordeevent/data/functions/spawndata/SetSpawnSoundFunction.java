package net.smileycorp.hordes.hordeevent.data.functions.spawndata;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;
import net.smileycorp.hordes.hordeevent.data.values.ValueGetter;

public class SetSpawnSoundFunction implements HordeFunction<HordeBuildSpawnDataEvent> {

    private final ValueGetter<String> getter;

    public SetSpawnSoundFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }

    @Override
    public void apply(HordeBuildSpawnDataEvent event) {
        event.getSpawnData().setSpawnSound(ResourceLocation.tryParse(getter.get(event)));
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
