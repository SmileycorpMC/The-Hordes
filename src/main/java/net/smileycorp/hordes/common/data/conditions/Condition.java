package net.smileycorp.hordes.common.data.conditions;

import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

public interface Condition {

	boolean apply(HordeContext<? extends HordePlayerEvent> ctx);

}
