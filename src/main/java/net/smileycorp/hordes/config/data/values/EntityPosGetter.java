package net.smileycorp.hordes.config.data.values;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataType;

import java.util.Random;

public class EntityPosGetter<T extends Comparable<T>, Number> implements ValueGetter<T> {
	
	private final ValueGetter<String> value;
	private final DataType<T> type;
	
	private EntityPosGetter(ValueGetter<String> value, DataType<T> type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public T get(World level, EntityLiving entity, EntityPlayerMP player, Random rand) {
		if (!type.isNumber()) return null;
		EnumFacing.Axis axis = EnumFacing.Axis.byName(value.get(level, entity, player, rand));
		if (type == DataType.INT || type == DataType.LONG) {
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
	
	public static <T extends Comparable<T>> ValueGetter deserialize(JsonObject object, DataType<T> type) {
		try {
			if (object.has("value")) return new EntityPosGetter(ValueGetter.readValue(DataType.STRING, object.get("value")), type);
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hordes:entity_pos", e);
		}
		return null;
	}
	
}
