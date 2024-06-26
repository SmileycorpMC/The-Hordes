package net.smileycorp.hordes.hordeevent.data.conditions;

import com.google.gson.JsonElement;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.values.ValueGetter;

public class LocalDifficultyCondition implements Condition {

	protected ValueGetter<Double> difficulty;

	public LocalDifficultyCondition(ValueGetter<Double> difficulty) {
		this.difficulty = difficulty;
	}

	@Override
	public boolean apply(HordePlayerEvent event) {
		return event.getLevel().getCurrentDifficultyAt(event.getPlayer().blockPosition()).getSpecialMultiplier() > difficulty.get(event);
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
