package net.smileycorp.hordes.config.data.hordeevent.functions.spawnentity;

import com.google.gson.JsonElement;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeSpawnEntityEvent;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;
import net.smileycorp.hordes.config.data.hordeevent.functions.HordeFunction;
import net.smileycorp.hordes.config.data.values.ValueGetter;

public class SetEntityTypeFunction implements HordeFunction<HordeSpawnEntityEvent> {
    
    private final ValueGetter<String> getter;
    
    public SetEntityTypeFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeContext<HordeSpawnEntityEvent> ctx) {
        String str = getter.get(ctx);
        try {
            EntityEntry type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(str));
            ctx.getEvent().setEntity((EntityLiving) type.newInstance(ctx.getWorld()));
        } catch (Exception e) {
            HordesLogger.logError("Failed changing entity " + ctx.getEntity() + " to type " + str, e);
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
