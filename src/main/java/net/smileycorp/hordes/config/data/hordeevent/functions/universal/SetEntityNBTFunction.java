package net.smileycorp.hordes.config.data.hordeevent.functions.universal;

import com.google.gson.JsonElement;
import net.minecraft.nbt.NBTTagCompound;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.DataRegistry;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;
import net.smileycorp.hordes.config.data.hordeevent.functions.HordeFunction;
import net.smileycorp.hordes.config.data.values.ValueGetter;

public class SetEntityNBTFunction implements HordeFunction<HordePlayerEvent> {
    
    private final ValueGetter<String> getter;
    
    public SetEntityNBTFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeContext<HordePlayerEvent> ctx) {
        String str = getter.get(ctx);
        try {
            NBTTagCompound nbt = DataRegistry.parseNBT(ctx.getEntity().toString(), str);
            ctx.getEntity().readFromNBT(nbt);
        } catch (Exception e) {
            HordesLogger.logError("Failed loading nbt " + str + " for entity " + ctx.getEntity(), e);
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
