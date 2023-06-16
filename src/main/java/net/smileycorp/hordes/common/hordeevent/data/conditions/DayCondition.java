package net.smileycorp.hordes.common.hordeevent.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Hordes;

public class DayCondition implements Condition {

	protected int day;

	public DayCondition(int day) {
		this.day = day;
	}

	@Override
	public boolean apply(Level level, Player player, RandomSource rand) {
		return level.getDayTime()/ (float) CommonConfigHandler.dayLength.get() > day;
	}

	public static DayCondition deserialize(JsonElement json) {
		try {
			return new DayCondition(json.getAsInt());
		} catch(Exception e) {
			Hordes.logError("Incorrect parameters for condition hordes:day", e);
		}
		return null;
	}

}
