package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;

import java.util.Random;

public class RandomCondition implements Condition {

	protected ValueGetter<Double> chance;

	public RandomCondition(ValueGetter<Double> chance) {
		this.chance = chance;
	}

	@Override
	public boolean apply(Level level, LivingEntity entity, ServerPlayer player, Random rand) {
		return rand.nextFloat() <= chance.get(level, entity, player, rand);
	}

	public static RandomCondition deserialize(JsonElement json) {
		try {
			return new RandomCondition(ValueGetter.readValue(DataType.DOUBLE, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:random", e);
		}
		return null;
	}

}
