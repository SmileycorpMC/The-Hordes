package net.smileycorp.hordes.common.hordeevent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;

public class HordeSpawnEntry {

	protected final EntityType<?> type;
	protected final int weight;
	protected final int minDay;
	protected final int maxDay;
	protected CompoundTag nbt = null;

	public HordeSpawnEntry(EntityType<?> type) {
		this(type, 0, 0, 0);
	}

	HordeSpawnEntry(EntityType<?> type, int weight, int minDay, int maxDay) {
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

	public EntityType<?> getEntity() {
		return type;
	}

	public CompoundTag getNBT() {
		return nbt == null ? new CompoundTag() : nbt;
	}

	public void setNBT(CompoundTag nbt) {
		this.nbt=nbt;
	}

	@Override
	public String toString() {
		String str = "HordeSpawnEntry[type="+type+",weight="+weight+",minDay="+minDay+",maxDay="+maxDay+"]";
		return nbt==null ? str : str + "{" + nbt.toString() + "}";
	}

}
