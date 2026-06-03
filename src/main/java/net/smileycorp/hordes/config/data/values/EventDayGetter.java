package net.smileycorp.hordes.config.data.values;


import com.google.gson.JsonObject;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.HordesParsingException;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;

public class EventDayGetter implements ValueGetter<Integer> {
	
	@Override
	public Integer get(HordeContext<? extends HordePlayerEvent> ctx) {
		return ctx.getDay();
	}
	
	public static <T extends Comparable<T>> ValueGetter deserialize(JsonObject object, DataType<T> type) {
		if (!type.isNumber()) {
			HordesLogger.logError("invalid value for hordes:day", new HordesParsingException("Expected type" + type + " is not a number"));
			return null;
		}
		return new EventDayGetter();
	}
	
}
