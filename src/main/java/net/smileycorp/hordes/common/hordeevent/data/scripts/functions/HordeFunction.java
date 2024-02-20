package net.smileycorp.hordes.common.hordeevent.data.scripts.functions;

import net.smileycorp.hordes.common.event.HordePlayerEvent;

public interface HordeFunction<T extends HordePlayerEvent> {

	public void apply(T event);

}