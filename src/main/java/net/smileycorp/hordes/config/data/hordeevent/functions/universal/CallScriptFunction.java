package net.smileycorp.hordes.config.data.hordeevent.functions.universal;

import com.google.gson.JsonElement;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;
import net.smileycorp.hordes.config.data.hordeevent.HordeScript;
import net.smileycorp.hordes.config.data.hordeevent.HordeScriptLoader;
import net.smileycorp.hordes.config.data.hordeevent.functions.HordeFunction;
import net.smileycorp.hordes.config.data.values.ValueGetter;

public class CallScriptFunction implements HordeFunction<HordePlayerEvent> {

    private final ValueGetter<String> getter;

    public CallScriptFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }
    
    @Override
    public void apply(HordeContext<HordePlayerEvent> ctx) {
        HordeScript script = HordeScriptLoader.INSTANCE.getScript(new ResourceLocation(getter.get(ctx)));
        if (script.getType() != ctx.getClass()) return;
        if (!script.shouldApply(ctx)) return;
        ctx.setCalled(true);
        script.apply(ctx);
        ctx.setCalled(false);
    }
    
    public static CallScriptFunction deserialize(JsonElement json) {
        try {
            return new CallScriptFunction(ValueGetter.readValue(DataType.STRING, json));
        } catch(Exception e) {
            HordesLogger.logError("Incorrect parameters for function hordes:call_script", e);
        }
        return null;
    }
    
}
