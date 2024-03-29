package net.smileycorp.hordes.hordeevent.data.functions;

import net.smileycorp.hordes.common.event.HordePlayerEvent;

public interface HordeFunction<T extends HordePlayerEvent> {

	void apply(T event);

}