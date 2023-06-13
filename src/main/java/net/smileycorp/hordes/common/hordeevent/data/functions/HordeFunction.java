package net.smileycorp.hordes.common.hordeevent.data.functions;

import net.smileycorp.hordes.common.event.HordeEvent;

public interface HordeFunction<T extends HordeEvent> {

	public void apply(T event);

}