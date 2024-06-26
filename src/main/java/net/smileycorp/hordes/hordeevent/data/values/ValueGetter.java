package net.smileycorp.hordes.hordeevent.data.values;

import com.google.gson.JsonElement;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.DataRegistry;

public interface ValueGetter<T extends Comparable<T>> {
    
    T get(HordePlayerEvent event);
    
    static <T extends Comparable<T>> ValueGetter<T> readValue(DataType<T> type, JsonElement value) {
        if (value.isJsonObject()) {
            return DataRegistry.readValue(type, value.getAsJsonObject());
        } else if (value.isJsonArray()) {
            return new RandomValue(type, value.getAsJsonArray());
        }
        T v = type.readFromJson(value);
        return e -> v;
    }


}
