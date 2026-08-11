package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonObject;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

public class GlobalGetter<T extends Comparable<T>> implements ValueGetter<T> {

	protected final ValueGetter<String> variable;
	private final DataType<T> type;

	public GlobalGetter(ValueGetter<String> variable, DataType<T> type) {
		this.variable = variable;
        this.type = type;
    }

	@Override
	public T get(HordeContext<? extends HordePlayerEvent> ctx) {
		return ctx.getGlobal(variable.get(ctx), type);
	}

	public static <T extends Comparable<T>> GlobalGetter<T> deserialize(JsonObject object, DataType<T> type) {
		try {
			if (object.has("value")) return new GlobalGetter<>(ValueGetter.readValue(DataType.STRING, object.get("value")), type);
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hordes:get_global", e);
		}
		return null;
	}

}
