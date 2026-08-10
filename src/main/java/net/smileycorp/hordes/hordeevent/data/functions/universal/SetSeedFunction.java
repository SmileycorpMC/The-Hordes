package net.smileycorp.hordes.hordeevent.data.functions.universal;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;
import net.smileycorp.hordes.hordeevent.data.HordeScript;
import net.smileycorp.hordes.hordeevent.data.HordeScriptLoader;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;

public class SetSeedFunction implements HordeFunction<HordePlayerEvent> {

    private final ValueGetter<Long> getter;

    public SetSeedFunction(ValueGetter<Long> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeContext<HordePlayerEvent> ctx) {
        ctx.getRandom().setSeed(getter.get(ctx));
    }
    
    public static SetSeedFunction deserialize(JsonElement json) {
        try {
            return new SetSeedFunction(ValueGetter.readValue(DataType.LONG, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:set_seed", e);
        }
        return null;
    }
    
}
