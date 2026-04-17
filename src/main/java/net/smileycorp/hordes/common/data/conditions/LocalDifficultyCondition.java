package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

public class LocalDifficultyCondition implements Condition {

	protected ValueGetter<Double> difficulty;

	public LocalDifficultyCondition(ValueGetter<Double> difficulty) {
		this.difficulty = difficulty;
	}

	@Override
	public boolean apply(HordeContext<? extends HordePlayerEvent> ctx) {
		return ctx.getWorld().getCurrentDifficultyAt(ctx.getPlayer().blockPosition()).getSpecialMultiplier() > difficulty.get(ctx);
	}

	public static LocalDifficultyCondition deserialize(JsonElement json) {
		try {
			return new LocalDifficultyCondition(ValueGetter.readValue(DataType.DOUBLE, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:local_difficulty", e);
		}
		return null;
	}

}
