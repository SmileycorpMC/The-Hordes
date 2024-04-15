package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.config.HordeEventConfig;

import java.util.Random;

public class PlayerDayCondition implements Condition {

	protected ValueGetter<Integer> day;

	public PlayerDayCondition(ValueGetter<Integer> day) {
		this.day = day;
	}

	@Override
	public boolean apply(World level, LivingEntity entity, ServerPlayerEntity player, Random rand) {
		return player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) / (float) HordeEventConfig.dayLength.get() > day.get(level, entity, player, rand);
	}

	public static PlayerDayCondition deserialize(JsonElement json) {
		try {
			return new PlayerDayCondition(ValueGetter.readValue(DataType.INT, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:player_day", e);
		}
		return null;
	}

}
