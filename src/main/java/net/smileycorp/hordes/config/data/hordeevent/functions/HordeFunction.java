package net.smileycorp.hordes.config.data.hordeevent.functions;

import net.smileycorp.hordes.common.event.HordePlayerEvent;

public interface HordeFunction<T extends HordePlayerEvent> {

	void apply(T event);

}