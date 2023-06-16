package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonElement;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;

public class StaticValue<T extends Comparable<T>> implements ValueGetter<T> {

    private final T value;

    public StaticValue(DataType<T> type, JsonElement value) {
        this.value = type.readFromJson(value);
    }

    @Override
    public T get(Level level, LivingEntity entity, RandomSource rand) {
        return value;
    }

}
