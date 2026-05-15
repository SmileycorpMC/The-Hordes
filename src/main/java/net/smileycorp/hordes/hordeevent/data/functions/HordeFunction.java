package net.smileycorp.hordes.hordeevent.data.functions;

import com.google.gson.JsonElement;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

public interface HordeFunction<T extends HordePlayerEvent> {

	void apply(HordeContext<T> ctx);

	interface Deserializer<T extends HordePlayerEvent> {

		HordeFunction<T> apply(JsonElement element);

	}

}