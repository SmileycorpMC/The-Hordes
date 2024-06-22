package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;

public class EntityTypeCondition implements Condition {

	protected ValueGetter<String> getter;

	public EntityTypeCondition(ValueGetter<String> getter) {
		this.getter = getter;
	}

	@Override
	public boolean apply(Level level, LivingEntity entity, ServerPlayer player, RandomSource rand) {
		ResourceLocation type = ResourceLocation.tryParse(getter.get(level, entity, player, rand));
		return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).equals(type);
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
