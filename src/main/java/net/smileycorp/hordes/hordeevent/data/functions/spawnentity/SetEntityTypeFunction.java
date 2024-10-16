package net.smileycorp.hordes.hordeevent.data.functions.spawnentity;

import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeSpawnEntityEvent;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;
import net.smileycorp.hordes.hordeevent.data.values.ValueGetter;

public class SetEntityTypeFunction implements HordeFunction<HordeSpawnEntityEvent> {
    
    private final ValueGetter<String> getter;
    
    public SetEntityTypeFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeSpawnEntityEvent event) {
        String str = getter.get(event);
        try {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(str));
            event.setEntity((Mob) type.create(event.getLevel()));
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
