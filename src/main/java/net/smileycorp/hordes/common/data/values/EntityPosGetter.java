package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;

public class EntityPosGetter<T extends Comparable<T>, Number> implements ValueGetter<T> {
	
	private final ValueGetter<String> value;
	private final DataType<T> type;
	
	private EntityPosGetter(ValueGetter<String> value, DataType<T> type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public T get(Level level, LivingEntity entity, RandomSource rand) {
		if (!type.isNumber()) return null;
		Axis axis = Axis.byName(value.get(level, entity, rand));
		if (type == DataType.INT) return type.cast(entity.blockPosition().get(axis));
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
