package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

public class DayCondition implements Condition {

	protected ValueGetter<Integer> day;

	public DayCondition(ValueGetter<Integer> day) {
		this.day = day;
	}

	@Override
	public boolean apply(HordeContext<? extends HordePlayerEvent> ctx) {
		return ctx.getDay() > day.get(ctx);
	}

	public static DayCondition deserialize(JsonElement json) {
		try {
			return new DayCondition(ValueGetter.readValue(DataType.INT, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:day", e);
		}
		return null;
	}

}
