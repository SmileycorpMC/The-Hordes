package net.smileycorp.hordes.common.data.values;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.hordes.common.HordesLogger;

import java.util.Map;
import java.util.Random;

public class WeightedRandomValueGetter<T extends Comparable<T>> implements ValueGetter<T> {
    
    private final WeightedOutputs<ValueGetter<T>> outputs;
    
    public WeightedRandomValueGetter(WeightedOutputs<ValueGetter<T>> outputs) {
        this.outputs = outputs;
    }
    
    @Override
    public T get(World level, LivingEntity entity, ServerPlayerEntity player, Random rand) {
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
