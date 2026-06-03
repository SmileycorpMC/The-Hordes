package net.smileycorp.hordes.config.data.values;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;

public class PlayerPosGetter<T extends Number & Comparable<T>> extends PosGetter<T> {

	public PlayerPosGetter(ValueGetter<String> value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected EntityLivingBase getEntity(HordeContext<? extends HordePlayerEvent> ctx) {
		return ctx.getPlayer();
	}
	
	public static <T extends Number & Comparable<T>> PlayerPosGetter<T> deserialize(JsonObject object, DataType<T> type) {
		try {
			if (object.has("value")) return new PlayerPosGetter<>(ValueGetter.readValue(DataType.STRING, object.get("value")), type);
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hordes:player_pos", e);
		}
		return null;
	}
	
}
