package net.smileycorp.hordes.config.data.values;


import com.google.gson.JsonObject;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.HordesParsingException;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;
import net.smileycorp.hordes.hordeevent.HordeSpawnData;

public class SpawnTableGetter implements ValueGetter<String> {

	@Override
	public String get(HordeContext<? extends HordePlayerEvent> ctx) {
		HordeSpawnData data = ctx.getSpawnData();
		return data == null ? null : data.getTable() == null ? null : data.getTable().getName().toString();
	}
	
	public static <T extends Comparable<T>> SpawnTableGetter deserialize(JsonObject object, DataType<T> type) {
		if (type != DataType.STRING) {
			HordesLogger.logError("invalid value for hordes:spawn_table", new HordesParsingException("Expected type" + type + " is not a string"));
			return null;
		}
		return new SpawnTableGetter();
	}
	
}
