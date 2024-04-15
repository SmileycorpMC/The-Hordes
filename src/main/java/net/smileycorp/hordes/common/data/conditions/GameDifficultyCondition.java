package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;

import java.util.Random;

public class GameDifficultyCondition implements Condition {

	protected ValueGetter<?> difficulty;

	public GameDifficultyCondition(ValueGetter<?> difficulty) {
		this.difficulty = difficulty;
	}

	@Override
	public boolean apply(World level, LivingEntity entity, ServerPlayerEntity player, Random rand) {
		Comparable value = difficulty.get(level, entity, player, rand);
		return level.getDifficulty() == (value instanceof String ? Difficulty.byName((String) value) : Difficulty.byId((Integer) value));
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
