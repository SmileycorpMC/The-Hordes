package net.smileycorp.hordes.config.data.hordeevent.functions.spawnentity;

import com.google.gson.JsonElement;
import net.minecraft.nbt.NBTTagCompound;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeSpawnEntityEvent;
import net.smileycorp.hordes.config.data.DataRegistry;
import net.smileycorp.hordes.config.data.DataType;
import net.smileycorp.hordes.config.data.hordeevent.functions.HordeFunction;
import net.smileycorp.hordes.config.data.values.ValueGetter;

public class SetEntityNBTFunction implements HordeFunction<HordeSpawnEntityEvent> {
    
    private final ValueGetter<String> getter;
    
    public SetEntityNBTFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeSpawnEntityEvent event) {
        String str = getter.get(event);
        try {
            NBTTagCompound nbt = DataRegistry.parseNBT(event.getEntity().toString(), str);
            event.getEntity().readFromNBT(nbt);
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
