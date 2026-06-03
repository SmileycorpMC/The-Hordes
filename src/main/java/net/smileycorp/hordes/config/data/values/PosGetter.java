package net.smileycorp.hordes.config.data.values;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;

public abstract class PosGetter<T extends Number & Comparable<T>> implements ValueGetter<T> {

	private final ValueGetter<String> value;
	private final DataType<T> type;

	protected PosGetter(ValueGetter<String> value, DataType<T> type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public T get(HordeContext<? extends HordePlayerEvent> ctx) {
		if (!type.isNumber()) return null;
		EnumFacing.Axis axis = EnumFacing.Axis.byName(value.get(ctx));
		EntityLivingBase entity = getEntity(ctx);
		if ((DataType<?>)type == DataType.INT || (DataType<?>)type == DataType.LONG) {
			BlockPos pos = entity.getPosition();
			switch (axis) {
				case X:
					return type.cast(pos.getX());
				case Y:
					return type.cast(pos.getY());
				default:
					return type.cast(pos.getZ());
			}
		}
		switch (axis) {
			case X:
				return type.cast(entity.posX);
			case Y:
				return type.cast(entity.posY);
			default:
				return type.cast(entity.posZ);
		}
	}

	protected abstract EntityLivingBase getEntity(HordeContext<? extends HordePlayerEvent> ctx);
	
}
