package net.smileycorp.hordes.hordeevent.data.conditions;

import net.smileycorp.hordes.common.event.HordePlayerEvent;

public interface Condition {

	boolean apply(HordePlayerEvent event);

}
