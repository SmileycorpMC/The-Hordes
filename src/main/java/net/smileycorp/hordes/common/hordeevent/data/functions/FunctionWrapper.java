package net.smileycorp.hordes.common.hordeevent.data.functions;

import net.smileycorp.hordes.common.event.HordeEvent;

public class FunctionWrapper<T extends HordeEvent> {

	protected final HordeFunction<T> func;
	protected final Class<T> type;

	public FunctionWrapper(HordeFunction<T> func, Class<T> type) {
		this.func = func;
		this.type = type;
	}

	public void apply(T event) {
		func.apply(event);
	}

	public Class<T> getType() {
		return type;
	}

}
