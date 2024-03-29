package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonElement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

public interface ValueGetter<T extends Comparable<T>> {

   T get(Level level, LivingEntity entity, ServerPlayer player, RandomSource rand);
    
    default T get(HordePlayerEvent event) {
        return get(event.getEntityWorld(), event.getEntity(), event.getPlayer(), event.getRandom());
    }
    
    static <T extends Comparable<T>> ValueGetter<T> readValue(DataType<T> type, JsonElement value) {
        if (value.isJsonObject()) {
            return DataRegistry.readValue(type, value.getAsJsonObject());
        } else if (value.isJsonArray()) {
            return new RandomValue(type, value.getAsJsonArray());
        }
        T v = type.readFromJson(value);
        return (l, e, p, r) -> v;
    }


}
