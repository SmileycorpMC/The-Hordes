package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonElement;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.data.DataRegistry;

public interface ValueGetter<T extends Comparable<T>> {

   T get(Level level, LivingEntity entity, RandomSource rand);

    static ValueGetter readValue(DataType type, JsonElement value) {
        if (value.isJsonObject()) {
            return DataRegistry.readValue(type, value.getAsJsonObject());
        } else if (value.isJsonArray()) {
            return new RandomValue(type, value.getAsJsonArray());
        }
        return new StaticValue(type, value);
    }


}
