package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.HordesLogger;

public class GameDifficultyCondition implements Condition {

	protected Difficulty difficulty;

	public GameDifficultyCondition(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	@Override
	public boolean apply(Level level, LivingEntity entity, RandomSource rand) {
		return level.getDifficulty() == difficulty;
	}

	public static GameDifficultyCondition deserialize(JsonElement json) {
		try {
			if (json.isJsonPrimitive()) {
				Difficulty difficulty = null;
				if (json.getAsJsonPrimitive().isString()) difficulty = Difficulty.byName(json.getAsString());
				if (json.getAsJsonPrimitive().isNumber()) difficulty = Difficulty.byId(json.getAsInt());
				if (difficulty == null) throw new NullPointerException();
				return new GameDifficultyCondition(difficulty);
			}
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:game_difficulty", e);
		}
		return null;
	}

}
