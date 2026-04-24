package net.smileycorp.hordes.hordeevent.data.functions;

import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

public interface HordeFunction<T extends HordePlayerEvent> {

	void apply(HordeContext<T> ctx);

}