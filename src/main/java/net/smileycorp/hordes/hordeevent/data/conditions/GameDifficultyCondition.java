package net.smileycorp.hordes.hordeevent.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.world.Difficulty;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.values.ValueGetter;

public class GameDifficultyCondition implements Condition {

	protected ValueGetter<?> difficulty;

	public GameDifficultyCondition(ValueGetter<?> difficulty) {
		this.difficulty = difficulty;
	}

	@Override
	public boolean apply(HordePlayerEvent event) {
		Comparable value = difficulty.get(event);
		return event.getLevel().getDifficulty() == (value instanceof String ? Difficulty.byName((String) value) : Difficulty.byId((Integer) value));
	}

	public static GameDifficultyCondition deserialize(JsonElement json) {
		try {
			ValueGetter getter;
			try {
				getter = ValueGetter.readValue(DataType.STRING, json);
			} catch (Exception e) {
				getter = ValueGetter.readValue(DataType.INT, json);
			}
			return new GameDifficultyCondition(getter);
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:game_difficulty", e);
		}
		return null;
	}

}
