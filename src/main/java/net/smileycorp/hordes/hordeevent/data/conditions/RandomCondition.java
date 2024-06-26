package net.smileycorp.hordes.hordeevent.data.conditions;

import com.google.gson.JsonElement;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.values.ValueGetter;

public class RandomCondition implements Condition {

	protected ValueGetter<Double> chance;

	public RandomCondition(ValueGetter<Double> chance) {
		this.chance = chance;
	}

	@Override
	public boolean apply(HordePlayerEvent event) {
		return event.getRandom().nextFloat() <= chance.get(event);
	}

	public static RandomCondition deserialize(JsonElement json) {
		try {
			return new RandomCondition(ValueGetter.readValue(DataType.DOUBLE, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:random", e);
		}
		return null;
	}

}
