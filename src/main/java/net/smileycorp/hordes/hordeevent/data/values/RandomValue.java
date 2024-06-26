package net.smileycorp.hordes.hordeevent.data.values;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

import java.util.List;

public class RandomValue<T extends Comparable<T>> implements ValueGetter<T> {

    private final List<ValueGetter<T>> values = Lists.newArrayList();

    public RandomValue(DataType<T> type, JsonArray json) {
        json.forEach(element -> values.add(ValueGetter.readValue(type, element)));
    }

    @Override
    public T get(HordePlayerEvent event) {
        return values.get(event.getRandom().nextInt(values.size())).get(event);
    }

}
