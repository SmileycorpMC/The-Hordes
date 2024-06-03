package net.smileycorp.hordes.config.data.values;

import com.google.gson.JsonElement;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.DataRegistry;
import net.smileycorp.hordes.config.data.DataType;

import java.util.Random;

public interface ValueGetter<T extends Comparable<T>> {

   T get(World level, EntityLivingBase entity, EntityPlayerMP player, Random rand);
    
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
