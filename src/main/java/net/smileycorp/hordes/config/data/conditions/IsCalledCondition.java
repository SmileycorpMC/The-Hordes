package net.smileycorp.hordes.config.data.conditions;

import com.google.gson.JsonElement;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;
import net.smileycorp.hordes.config.data.values.ValueGetter;

public class IsCalledCondition implements Condition {

	protected ValueGetter<Boolean> isCalled;

	public IsCalledCondition(ValueGetter<Boolean> isCalled) {
		this.isCalled = isCalled;
	}

	@Override
	public boolean apply(HordeContext<? extends HordePlayerEvent> ctx) {
		return ctx.isCalled() == isCalled.get(ctx);
	}

	public static IsCalledCondition deserialize(JsonElement json) {
		try {
			return new IsCalledCondition(ValueGetter.readValue(DataType.BOOLEAN, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:is_called", e);
		}
		return null;
	}

}
