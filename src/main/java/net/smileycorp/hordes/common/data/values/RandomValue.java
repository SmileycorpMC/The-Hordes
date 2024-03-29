package net.smileycorp.hordes.common.data.values;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;

import java.util.List;

public class RandomValue<T extends Comparable<T>> implements ValueGetter<T> {

    private final List<ValueGetter<T>> values = Lists.newArrayList();

    public RandomValue(DataType<T> type, JsonArray json) {
        json.forEach(element -> values.add(ValueGetter.readValue(type, element)));
    }

    @Override
    public T get(Level level, LivingEntity entity, RandomSource rand) {
        return values.get(rand.nextInt(values.size())).get(level, entity, rand);
    }

}
