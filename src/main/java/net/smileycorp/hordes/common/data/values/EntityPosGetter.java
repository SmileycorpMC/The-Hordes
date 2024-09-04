package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;

import java.util.Random;

public class EntityPosGetter<T extends Comparable<T>, Number> implements ValueGetter<T> {
	
	private final ValueGetter<String> value;
	private final DataType<T> type;
	
	private EntityPosGetter(ValueGetter<String> value, DataType<T> type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public T get(World level, LivingEntity entity, ServerPlayerEntity player, Random rand) {
		if (!type.isNumber()) return null;
		Direction.Axis axis = Direction.Axis.byName(value.get(level, entity, player, rand));
		if (type == DataType.INT || type == DataType.LONG) return type.cast(entity.blockPosition().get(axis));
		return type.cast(entity.position().get(axis));
	}
	
	public static <T extends Comparable<T>> ValueGetter deserialize(JsonObject object, DataType<T> type) {
		try {
			if (object.has("value")) return new EntityPosGetter(ValueGetter.readValue(DataType.STRING, object.get("value")), type);
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hordes:entity_pos", e);
		}
		return null;
	}
	
}
