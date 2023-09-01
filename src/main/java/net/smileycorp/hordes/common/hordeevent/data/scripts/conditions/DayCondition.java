package net.smileycorp.hordes.common.hordeevent.data.scripts.conditions;

import com.google.gson.JsonElement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Hordes;

import java.util.Random;

public class DayCondition implements Condition {

	protected int day;

	public DayCondition(int day) {
		this.day = day;
	}

	@Override
	public boolean apply(Level level, Player player, Random rand) {
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
