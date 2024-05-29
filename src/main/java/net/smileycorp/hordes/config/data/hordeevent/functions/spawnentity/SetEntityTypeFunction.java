package net.smileycorp.hordes.config.data.hordeevent.functions.spawnentity;

import com.google.gson.JsonElement;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordeSpawnEntityEvent;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;

public class SetEntityTypeFunction implements HordeFunction<HordeSpawnEntityEvent> {
    
    private final ValueGetter<String> getter;
    
    public SetEntityTypeFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeSpawnEntityEvent event) {
        String str = getter.get(event);
        try {
            EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(str));
            event.setEntity((MobEntity) type.create(event.getEntityWorld()));
        } catch (Exception e) {
            HordesLogger.logError("Failed changing entity " + event.getEntity() + " to type " + str, e);
        }
    }
    
    public static SetEntityTypeFunction deserialize(JsonElement json) {
        try {
            return new SetEntityTypeFunction(ValueGetter.readValue(DataType.STRING, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_entity_type", e);
        }
        return null;
    }
    
}
