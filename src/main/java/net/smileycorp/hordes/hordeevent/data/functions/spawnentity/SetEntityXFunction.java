package net.smileycorp.hordes.hordeevent.data.functions.spawnentity;

import com.google.gson.JsonElement;
import net.minecraft.world.phys.Vec3;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeSpawnEntityEvent;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;
import net.smileycorp.hordes.hordeevent.data.values.ValueGetter;

public class SetEntityXFunction implements HordeFunction<HordeSpawnEntityEvent> {
    
    private final ValueGetter<Double> getter;
    
    public SetEntityXFunction(ValueGetter<Double> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeSpawnEntityEvent event) {
        Vec3 pos = event.getPos();
        event.setPos(new Vec3(getter.get(event), pos.y(), pos.z()));
    }
    
    public static SetEntityXFunction deserialize(JsonElement json) {
        try {
            return new SetEntityXFunction(ValueGetter.readValue(DataType.DOUBLE, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_entity_x", e);
        }
        return null;
    }
    
}
