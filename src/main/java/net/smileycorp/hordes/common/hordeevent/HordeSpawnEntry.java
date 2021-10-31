package net.smileycorp.hordes.common.hordeevent;

import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;

public class HordeSpawnEntry {

	protected final EntityType<?> type;
	protected final int weight;
	protected final int minDay;
	protected final int maxDay;
	protected CompoundNBT nbt = null;

	public HordeSpawnEntry(EntityType<?> type, int weight, int minDay, int maxDay) {
		this.type=type;
		this.weight=weight;
		this.minDay=minDay;
		this.maxDay=maxDay;
	}

	public int getWeight() {
		return weight;
	}

	public int getMinDay() {
		return minDay;
	}

	public int getMaxDay() {
		return maxDay;
	}

	@Override
	public String toString() {
		String str = "HordeSpawnEntry[type="+type+",weight="+weight+",minDay="+minDay+",maxDay="+maxDay+"]";
		return nbt==null ? str : str + "{" + nbt.toString() + "}";
	}

	public void setTagCompound(CompoundNBT nbt) {
		this.nbt=nbt;
	}

	public CompoundNBT getNBT() {
		return nbt == null ? new CompoundNBT() : nbt;
	}

}
