package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.HordesLogger;

public class LocalDifficultyCondition implements Condition {

	protected float difficulty;

	public LocalDifficultyCondition(float difficulty) {
		this.difficulty = difficulty;
	}

	@Override
	public boolean apply(Level level, LivingEntity entity, RandomSource rand) {
		return level.getCurrentDifficultyAt(entity.blockPosition()).getSpecialMultiplier() > difficulty;
	}

	public static LocalDifficultyCondition deserialize(JsonElement json) {
		try {
			return new LocalDifficultyCondition(json.getAsFloat());
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:local_difficulty", e);
		}
		return null;
	}

}
