package net.smileycorp.hordes.hordeevent.data.values;

import com.google.gson.JsonObject;
import net.minecraft.core.Direction.Axis;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

public class PlayerPosGetter<T extends Comparable<T>, Number> implements ValueGetter<T> {
	
	private final ValueGetter<String> value;
	private final DataType<T> type;
	
	private PlayerPosGetter(ValueGetter<String> value, DataType<T> type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public T get(HordePlayerEvent event) {
		if (!type.isNumber()) return null;
		Axis axis = Axis.byName(value.get(event));
		if (type == DataType.INT || type == DataType.LONG) return type.cast(event.getPlayer().blockPosition().get(axis));
		return type.cast(event.getPlayer().position().get(axis));
	}
	
	public static <T extends Comparable<T>> ValueGetter deserialize(JsonObject object, DataType<T> type) {
		try {
			if (object.has("value")) return new PlayerPosGetter(ValueGetter.readValue(DataType.STRING, object.get("value")), type);
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hordes:player_pos", e);
		}
		return null;
	}
	
}
