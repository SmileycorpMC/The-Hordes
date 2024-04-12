package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
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
	public boolean apply(Level level, LivingEntity entity, ServerPlayer player, Random rand) {
		return player.tickCount / (float) HordeEventConfig.dayLength.get() > day.get(level, entity, player, rand);
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
