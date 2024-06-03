package net.smileycorp.hordes.config.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.StatList;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.config.data.DataType;
import net.smileycorp.hordes.config.data.values.ValueGetter;

import java.util.Random;

public class PlayerDayCondition implements Condition {

	protected ValueGetter<Integer> day;

	public PlayerDayCondition(ValueGetter<Integer> day) {
		this.day = day;
	}

	@Override
	public boolean apply(World level, EntityLivingBase entity, EntityPlayerMP player, Random rand) {
		return player.getStatFile().readStat(StatList.PLAY_ONE_MINUTE) / (float) HordeEventConfig.dayLength > day.get(level, entity, player, rand);
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
