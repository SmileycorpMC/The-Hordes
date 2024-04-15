package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonElement;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

import java.util.Random;

public interface ValueGetter<T extends Comparable<T>> {

   T get(World level, LivingEntity entity, ServerPlayerEntity player, Random rand);
    
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
