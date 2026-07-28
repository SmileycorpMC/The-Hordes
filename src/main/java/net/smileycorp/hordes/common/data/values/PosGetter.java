package net.smileycorp.hordes.common.data.values;

import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.LivingEntity;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

public abstract class PosGetter<T extends Comparable<T>> implements ValueGetter<T> {

	private final ValueGetter<String> value;
	private final DataType<T> type;

	protected PosGetter(ValueGetter<String> value, DataType<T> type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public T get(HordeContext<? extends HordePlayerEvent> ctx) {
		if (!type.isNumber()) return null;
		Axis axis = Axis.byName(value.get(ctx));
		LivingEntity entity = getEntity(ctx);
		if (type == DataType.INT || type == DataType.LONG) return type.cast(entity.blockPosition().get(axis));
		return type.cast(entity.position().get(axis));
	}

	protected abstract LivingEntity getEntity(HordeContext<? extends HordePlayerEvent> ctx);
	
}
