package net.smileycorp.hordes.hordeevent.data.values;


import com.google.gson.JsonObject;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

public class EventDayGetter implements ValueGetter<Integer> {
	

	@Override
	public Integer get(HordePlayerEvent event) {
		return event.getDay();
	}
	
	public static <T extends Comparable<T>> ValueGetter deserialize(JsonObject object, DataType<T> type) {
		return type.isNumber() ? new EventDayGetter() : null;
	}
	
}
