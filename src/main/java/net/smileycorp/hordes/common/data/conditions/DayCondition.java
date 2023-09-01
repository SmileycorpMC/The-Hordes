package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.HordesLogger;

public class DayCondition implements Condition {

	protected int day;

	public DayCondition(int day) {
		this.day = day;
	}

	@Override
	public boolean apply(Level level, LivingEntity entity, RandomSource rand) {
		return level.getDayTime()/ (float) CommonConfigHandler.dayLength.get() > day;
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
