package net.smileycorp.hordes.config.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataType;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.config.data.values.ValueGetter;

import java.util.Random;

public class AdvancementCondition implements Condition {

	protected ValueGetter<String> getter;

	public AdvancementCondition(ValueGetter<String> getter) {
		this.getter = getter;
	}

	@Override
	public boolean apply(World level, EntityLiving entity, EntityPlayerMP player, Random rand) {
		ResourceLocation advancement = new ResourceLocation(getter.get(level, entity, player, rand));
		return player.getAdvancements().getProgress(player.getServer().getAdvancementManager().getAdvancement(advancement)).isDone();
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
