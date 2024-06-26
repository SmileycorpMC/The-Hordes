package net.smileycorp.hordes.hordeevent.data.values;

import net.minecraft.nbt.CompoundTag;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.NBTExplorer;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

public abstract class NBTGetter<T extends Comparable<T>> implements ValueGetter<T> {

	protected final ValueGetter<String> value;
	private final DataType<T> type;
	
	public NBTGetter(ValueGetter<String> value, DataType<T> type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public T get(HordePlayerEvent event) {
		try {
			return new NBTExplorer<>(value.get(event), type).findValue(getNBT(event));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected abstract CompoundTag getNBT(HordePlayerEvent event);

}
