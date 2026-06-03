package net.smileycorp.hordes.config.data.values;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.NBTExplorer;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;

public abstract class NBTGetter<T extends Comparable<T>> implements ValueGetter<T> {

	protected final ValueGetter<String> value;
	private final DataType<T> type;
	
	public NBTGetter(ValueGetter<String> value, DataType<T> type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public T get(HordeContext<? extends HordePlayerEvent> ctx) {
		try {
			return new NBTExplorer<>(value.get(ctx), type).findValue(getNBT(ctx));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected abstract NBTTagCompound getNBT(HordeContext<? extends HordePlayerEvent> ctx);

}
