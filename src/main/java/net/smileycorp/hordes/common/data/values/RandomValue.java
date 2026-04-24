package net.smileycorp.hordes.common.data.values;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

import java.util.List;

public class RandomValue<T extends Comparable<T>> implements ValueGetter<T> {

    private final List<ValueGetter<T>> values = Lists.newArrayList();

    public RandomValue(DataType<T> type, JsonArray json) {
        json.forEach(element -> { try {
            values.add(ValueGetter.readValue(type, element));
        } catch (Exception e) {
            HordesLogger.logError("Error loading value hordes:random", e);
        }});
    }

    @Override
    public T get(HordeContext<? extends HordePlayerEvent> ctx) {
        return values.get(ctx.getRandom().nextInt(values.size())).get(ctx);
    }

}
