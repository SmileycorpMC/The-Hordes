package net.smileycorp.hordes.config.data.hordeevent.functions;

import com.google.gson.JsonElement;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

public interface HordeFunction<T extends HordePlayerEvent> {

	void apply(T event);
	
	interface Deserializer<T extends HordePlayerEvent> {
		
		HordeFunction<T> apply(JsonElement element);
		
	}
}