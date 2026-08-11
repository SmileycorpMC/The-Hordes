package net.smileycorp.hordes.hordeevent.data.functions.universal;

import com.google.gson.JsonElement;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;

public class RandomSeedFunction implements HordeFunction<HordePlayerEvent> {
    
    @Override
    public void apply(HordeContext<HordePlayerEvent> ctx) {
        ctx.getRandom().setSeed(RandomSupport.generateUniqueSeed());
    }
    
    public static RandomSeedFunction deserialize(JsonElement json) {
        return new RandomSeedFunction();
    }
    
}
