package net.smileycorp.hordes.hordeevent.data.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.smileycorp.atlas.api.data.ComparableOperation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.values.ValueGetter;

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
	public boolean apply(HordePlayerEvent event) {
		return operation.apply(value1.get(event), value2.get(event));
	}

	public static ComparisonCondition deserialize(JsonElement json) {
		try {
			JsonObject obj = json.getAsJsonObject();
			DataType type = DataType.of(obj.get("type").getAsString());
			ComparableOperation operation = ComparableOperation.of(obj.get("operation").getAsString());
			ValueGetter value1 = ValueGetter.readValue(type,  obj.get("value1"));
			ValueGetter value2 = ValueGetter.readValue(type,  obj.get("value2"));
			return new ComparisonCondition(value1, operation, value2);
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:comparison", e);
		}
		return null;
	}

}
