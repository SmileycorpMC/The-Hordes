package net.smileycorp.hordes.config.data.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.smileycorp.atlas.api.data.ComparableOperation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;
import net.smileycorp.hordes.config.data.values.ValueGetter;

public class ComparisonCondition<T extends Comparable<T>> implements Condition {

	protected final ValueGetter<T> value1;
	protected final ComparableOperation operation;
	protected final ValueGetter<T> value2;

	private ComparisonCondition(ValueGetter<T> value1, ComparableOperation operation, ValueGetter<T> value2) {
		this.value1 = value1;
		this.operation = operation;
		this.value2 = value2;
	}

	@Override
	public boolean apply(HordeContext<? extends HordePlayerEvent> ctx) {
		return operation.apply(value1.get(ctx), value2.get(ctx));
	}

	public static <T extends Comparable<T>> ComparisonCondition<T> deserialize(JsonElement json) {
		try {
			JsonObject obj = json.getAsJsonObject();
			DataType<T> type = (DataType<T>) DataType.of(obj.get("type").getAsString());
			ComparableOperation operation = ComparableOperation.of(obj.get("operation").getAsString());
			ValueGetter<T> value1 = ValueGetter.readValue(type,  obj.get("value1"));
			ValueGetter<T> value2 = ValueGetter.readValue(type,  obj.get("value2"));
			return new ComparisonCondition<>(value1, operation, value2);
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:comparison", e);
		}
		return null;
	}

}
