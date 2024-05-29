package net.smileycorp.hordes.config.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.config.data.DataRegistry;

import java.util.Random;

public class NotCondition implements Condition {

	protected Condition condition;

	public NotCondition(Condition condition) {
		this.condition = condition;
	}

	@Override
	public boolean apply(World level, EntityLiving entity, EntityPlayerMP player, Random rand) {
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
