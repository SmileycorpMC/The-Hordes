package net.smileycorp.hordes.hordeevent.data.values;


import com.google.gson.JsonObject;
import net.minecraft.world.level.SpawnData;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.HordeSpawnData;

public class SpawnTableGetter implements ValueGetter<String> {

	@Override
	public String get(HordePlayerEvent event) {
		HordeSpawnData data = event.getSpawnData();
		return data == null ? null : data.getTable() == null ? null : data.getTable().getName().toString();
	}
	
	public static <T extends Comparable<T>> ValueGetter deserialize(JsonObject object, DataType<T> type) {
		return type == DataType.STRING ? new SpawnTableGetter() : null;
	}
	
}
