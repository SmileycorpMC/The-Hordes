package net.smileycorp.hordes.config.data.hordeevent.functions.spawnentity;

import com.google.gson.JsonElement;
import net.minecraft.util.math.Vec3d;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordeSpawnEntityEvent;
import net.smileycorp.hordes.config.data.DataType;
import net.smileycorp.hordes.config.data.hordeevent.functions.HordeFunction;
import net.smileycorp.hordes.config.data.values.ValueGetter;

public class SetEntityYFunction implements HordeFunction<HordeSpawnEntityEvent> {
    
    private final ValueGetter<Double> getter;
    
    public SetEntityYFunction(ValueGetter<Double> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeSpawnEntityEvent event) {
        Vec3d pos = event.getPos();
        event.setPos(new Vec3d(pos.x, getter.get(event), pos.z));
    }
    
    public static SetEntityYFunction deserialize(JsonElement json) {
        try {
            return new SetEntityYFunction(ValueGetter.readValue(DataType.DOUBLE, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_entity_y", e);
        }
        return null;
    }
    
}
