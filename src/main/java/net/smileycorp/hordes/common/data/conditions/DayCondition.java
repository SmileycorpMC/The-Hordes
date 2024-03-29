package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.config.HordeEventConfig;

public class DayCondition implements Condition {

	protected int day;

	public DayCondition(int day) {
		this.day = day;
	}

	@Override
	public boolean apply(Level level, LivingEntity entity, ServerPlayer player, RandomSource rand) {
		return level.getDayTime() / (float) HordeEventConfig.dayLength.get() > day;
	}

	public static DayCondition deserialize(JsonElement json) {
		try {
			return new DayCondition(json.getAsInt());
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:day", e);
		}
		return null;
	}

}
