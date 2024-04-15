package net.smileycorp.hordes.hordeevent.data.functions.spawndata;

import com.google.gson.JsonElement;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.hordeevent.data.HordeTableLoader;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;

public class SetSpawntableFunction implements HordeFunction<HordeBuildSpawnDataEvent> {

    private final ValueGetter<String> getter;

    public SetSpawntableFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }

    @Override
    public void apply(HordeBuildSpawnDataEvent event) {
        event.getSpawnData().setTable(HordeTableLoader.INSTANCE.getTable(new ResourceLocation(getter.get(event))));
    }

    public static SetSpawntableFunction deserialize(JsonElement json) {
        try {
            return new SetSpawntableFunction(ValueGetter.readValue(DataType.STRING, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_spawntable", e);
        }
        return null;
    }
    
}
