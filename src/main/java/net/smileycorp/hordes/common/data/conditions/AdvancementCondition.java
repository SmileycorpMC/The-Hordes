package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;

import java.util.Random;

public class AdvancementCondition implements Condition {

	protected ValueGetter<String> getter;

	public AdvancementCondition(ValueGetter<String> getter) {
		this.getter = getter;
	}

	@Override
	public boolean apply(Level level, LivingEntity entity, ServerPlayer player, Random rand) {
		ResourceLocation advancement = new ResourceLocation(getter.get(level, entity, player, rand));
		return player.getAdvancements().getOrStartProgress(player.getServer().getAdvancements().getAdvancement(advancement)).isDone();
	}

	public static AdvancementCondition deserialize(JsonElement json) {
		try {
			return new AdvancementCondition(ValueGetter.readValue(DataType.STRING, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:advancement", e);
		}
		return null;
	}

}
