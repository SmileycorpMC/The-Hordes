package net.smileycorp.hordes.hordeevent.data.functions.spawnentity;

import com.google.gson.JsonElement;
import net.minecraft.nbt.CompoundTag;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordeSpawnEntityEvent;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;

public class SetEntityNBTFunction implements HordeFunction<HordeSpawnEntityEvent> {
    
    private final ValueGetter<String> getter;
    
    public SetEntityNBTFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeSpawnEntityEvent event) {
        String str = getter.get(event);
        try {
            CompoundTag nbt = DataRegistry.parseNBT(event.getEntity().toString(), str);
            event.getEntity().readAdditionalSaveData(nbt);
        } catch (Exception e) {
            HordesLogger.logError("Failed loading nbt " + str + " for entity " + event.getEntity(), e);
        }
    }
    
    public static SetEntityNBTFunction deserialize(JsonElement json) {
        try {
            return new SetEntityNBTFunction(ValueGetter.readValue(DataType.STRING, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_entity_nbt", e);
        }
        return null;
    }
    
}
