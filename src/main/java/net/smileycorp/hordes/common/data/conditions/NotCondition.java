package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataRegistry;

import java.util.Random;

public class NotCondition implements Condition {

	protected Condition condition;

	public NotCondition(Condition condition) {
		this.condition = condition;
	}

	@Override
	public boolean apply(Level level, LivingEntity entity, ServerPlayer player, Random rand) {
		return !condition.apply(level, entity, player, rand);
	}

	public static NotCondition deserialize(JsonElement json) {
		try {
			return new NotCondition(DataRegistry.readCondition(json.getAsJsonObject()));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:not", e);
		}
		return null;
	}

}
