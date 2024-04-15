package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;

import java.util.Random;

public class EntityTypeCondition implements Condition {

	protected ValueGetter<String> getter;

	public EntityTypeCondition(ValueGetter<String> getter) {
		this.getter = getter;
	}

	@Override
	public boolean apply(World level, LivingEntity entity, ServerPlayerEntity player, Random rand) {
		ResourceLocation type = new ResourceLocation(getter.get(level, entity, player, rand));
		return ForgeRegistries.ENTITIES.getKey(entity.getType()).equals(type);
	}

	public static EntityTypeCondition deserialize(JsonElement json) {
		try {
			return new EntityTypeCondition(ValueGetter.readValue(DataType.STRING, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:entity_type", e);
		}
		return null;
	}

}
