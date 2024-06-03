package net.smileycorp.hordes.config.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.config.data.DataType;
import net.smileycorp.hordes.config.data.values.ValueGetter;

import java.util.Locale;
import java.util.Random;

public class GameDifficultyCondition implements Condition {

	protected ValueGetter<?> difficulty;

	public GameDifficultyCondition(ValueGetter<?> difficulty) {
		this.difficulty = difficulty;
	}

	@Override
	public boolean apply(World level, EntityLivingBase entity, EntityPlayerMP player, Random rand) {
		Comparable value = difficulty.get(level, entity, player, rand);
		return level.getDifficulty() == (value instanceof String ? EnumDifficulty.valueOf(((String) value).toUpperCase(Locale.US))
				: EnumDifficulty.getDifficultyEnum((Integer) value));
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
