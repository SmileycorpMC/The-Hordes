package net.smileycorp.hordes.config.data.hordeevent.functions.spawnentity;

import com.google.gson.JsonElement;
import net.minecraft.nbt.CompoundNBT;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordeSpawnEntityEvent;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;

public class SetEntityLootTableFunction implements HordeFunction<HordeSpawnEntityEvent> {
    
    private final ValueGetter<String> getter;
    
    public SetEntityLootTableFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeSpawnEntityEvent event) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("DeathLootTable", getter.get(event));
        event.getEntity().readAdditionalSaveData(tag);
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
