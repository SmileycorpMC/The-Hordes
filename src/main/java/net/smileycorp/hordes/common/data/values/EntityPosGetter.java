package net.smileycorp.hordes.common.data.values;

import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;

public class EntityPosGetter<T extends Comparable<T>, Number> implements ValueGetter<T> {

	private final Axis axis;
	private final DataType<T> type;

	public EntityPosGetter(String value, DataType<T> type) {
		this.axis = Axis.byName(value);
		this.type = type;
	}

	@Override
	public T get(Level level, LivingEntity entity, RandomSource rand) {
		if (!type.isNumber()) return null;
		if (type == DataType.INT) return type.cast(entity.blockPosition().get(axis));
		return type.cast(entity.position().get(axis));
	}
}
