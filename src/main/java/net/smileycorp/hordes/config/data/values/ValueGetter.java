package net.smileycorp.hordes.config.data.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.DataRegistry;
import net.smileycorp.hordes.config.data.HordesParsingException;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;

public interface ValueGetter<T extends Comparable<T>> {

    T get(HordeContext<? extends HordePlayerEvent> ctx);

    static <T extends Comparable<T>> ValueGetter<T> readValue(DataType<T> type, JsonElement value) throws Exception {
        if (value instanceof JsonNull) throw new HordesParsingException("No value present");
        if (value.isJsonObject()) {
            return DataRegistry.readValue(type, value.getAsJsonObject());
        } else if (value.isJsonArray()) {
            return new RandomValue(type, value.getAsJsonArray());
        }
        T v = type.readFromJson(value);
        return ctx -> v;
    }

}
