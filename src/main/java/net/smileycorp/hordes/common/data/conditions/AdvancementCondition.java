package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.HordesLogger;

public class AdvancementCondition implements Condition {

	protected ResourceLocation advancement;

	public AdvancementCondition(ResourceLocation advancement) {
		this.advancement = advancement;
	}

	@Override
	public boolean apply(Level level, LivingEntity entity, RandomSource rand) {
		if (!(entity instanceof ServerPlayer)) return false;
		ServerPlayer player = (ServerPlayer) entity;
		return player.getAdvancements().getOrStartProgress(player.getServer().getAdvancements().getAdvancement(advancement)).isDone();
	}

	public static AdvancementCondition deserialize(JsonElement json) {
		try {
			return new AdvancementCondition(new ResourceLocation(json.getAsString()));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:advancement", e);
		}
		return null;
	}

}
