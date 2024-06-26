package net.smileycorp.hordes.hordeevent.data.functions.spawndata;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeBuildSpawnDataEvent;
import net.smileycorp.hordes.hordeevent.data.HordeTableLoader;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;
import net.smileycorp.hordes.hordeevent.data.values.ValueGetter;

public class SetSpawntableFunction implements HordeFunction<HordeBuildSpawnDataEvent> {

    private final ValueGetter<String> getter;

    public SetSpawntableFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }

    @Override
    public void apply(HordeBuildSpawnDataEvent event) {
        event.getSpawnData().setTable(HordeTableLoader.INSTANCE.getTable(ResourceLocation.tryParse(getter.get(event))));
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
