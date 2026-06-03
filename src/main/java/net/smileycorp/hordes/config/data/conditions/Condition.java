package net.smileycorp.hordes.config.data.conditions;

import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;

public interface Condition {
	
	boolean apply(HordeContext<? extends HordePlayerEvent> ctx);

}
