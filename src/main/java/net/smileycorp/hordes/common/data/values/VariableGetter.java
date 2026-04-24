package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonObject;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

public class VariableGetter<T extends Comparable<T>> implements ValueGetter<T> {

	protected final ValueGetter<String> variable;

	public VariableGetter(ValueGetter<String> variable) {
		this.variable = variable;
	}

	@Override
	public T get(HordeContext<? extends HordePlayerEvent> ctx) {
		return (T) ctx.getValue(variable.get(ctx));
	}

	public static <T extends Comparable<T>> VariableGetter<T> deserialize(JsonObject object, DataType<T> type) {
		try {
			if (object.has("value")) return new VariableGetter<>(ValueGetter.readValue(DataType.STRING, object.get("value")));
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hordes:get_variable", e);
		}
		return null;
	}

}
