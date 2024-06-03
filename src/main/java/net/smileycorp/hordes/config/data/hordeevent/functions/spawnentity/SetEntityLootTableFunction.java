package net.smileycorp.hordes.config.data.hordeevent.functions.spawnentity;

import com.google.gson.JsonElement;
import net.minecraft.nbt.NBTTagCompound;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeSpawnEntityEvent;
import net.smileycorp.hordes.config.data.DataType;
import net.smileycorp.hordes.config.data.hordeevent.functions.HordeFunction;
import net.smileycorp.hordes.config.data.values.ValueGetter;

public class SetEntityLootTableFunction implements HordeFunction<HordeSpawnEntityEvent> {
    
    private final ValueGetter<String> getter;
    
    public SetEntityLootTableFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeSpawnEntityEvent event) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("DeathLootTable", getter.get(event));
        event.getEntity().readFromNBT(tag);
    }
    
    public static SetEntityLootTableFunction deserialize(JsonElement json) {
        try {
            return new SetEntityLootTableFunction(ValueGetter.readValue(DataType.STRING, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_entity_loot_table", e);
        }
        return null;
    }
    
}
