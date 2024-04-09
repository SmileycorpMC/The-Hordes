package net.smileycorp.hordes.common.data.values;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.util.WeightedOutputs;
import net.smileycorp.hordes.common.HordesLogger;

import java.util.Map;

public class WeightedRandomValueGetter<T extends Comparable<T>> implements ValueGetter<T> {
    
    private final WeightedOutputs<ValueGetter<T>> outputs;
    
    public WeightedRandomValueGetter(WeightedOutputs<ValueGetter<T>> outputs) {
        this.outputs = outputs;
    }
    
    @Override
    public T get(Level level, LivingEntity entity, ServerPlayer player, RandomSource rand) {
        return outputs.getResult(rand).get(level, entity, player, rand);
    }
    
    public static <T extends Number & Comparable<T>> WeightedRandomValueGetter deserialize(JsonObject json, DataType<T> type) {
        Map<ValueGetter<T>, Integer> values = Maps.newHashMap();
        for (JsonElement element : json.get("value").getAsJsonArray()) {
            try {
                JsonObject entry = element.getAsJsonObject();
                ValueGetter<T> getter = ValueGetter.readValue(type, entry.get("value"));
                if (getter != null) values.put(getter, entry.get("weight").getAsInt());
            } catch (Exception e) {
                HordesLogger.logError("invalid entry for " + element + " for hordes:weighted_random", e);
            }
        }
        return new WeightedRandomValueGetter(new WeightedOutputs(values));
    }
    
}
