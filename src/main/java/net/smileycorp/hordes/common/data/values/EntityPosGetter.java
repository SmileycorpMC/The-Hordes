package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.LivingEntity;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

public class EntityPosGetter<T extends Comparable<T>> extends PosGetter<T> {
	
	private EntityPosGetter(ValueGetter<String> value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected LivingEntity getEntity(HordeContext<? extends HordePlayerEvent> ctx) {
		return ctx.getEntity();
	}

	public static <T extends Number & Comparable<T>> EntityPosGetter<T> deserialize(JsonObject object, DataType<T> type) {
		try {
			if (object.has("value")) return new EntityPosGetter<>(ValueGetter.readValue(DataType.STRING, object.get("value")), type);
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hordes:entity_pos", e);
		}
		return null;
	}
	
}
