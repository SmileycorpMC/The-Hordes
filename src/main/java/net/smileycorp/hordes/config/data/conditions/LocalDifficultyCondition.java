package net.smileycorp.hordes.config.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataType;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.config.data.values.ValueGetter;

import java.util.Random;

public class LocalDifficultyCondition implements Condition {

	protected ValueGetter<Double> difficulty;

	public LocalDifficultyCondition(ValueGetter<Double> difficulty) {
		this.difficulty = difficulty;
	}

	@Override
	public boolean apply(World level, EntityLiving entity, EntityPlayerMP player, Random rand) {
		return level.getDifficultyForLocation(player.getPosition()).getClampedAdditionalDifficulty() > difficulty.get(level, entity, player, rand);
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
