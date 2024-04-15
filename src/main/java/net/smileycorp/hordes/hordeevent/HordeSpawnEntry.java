package net.smileycorp.hordes.hordeevent;

import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class HordeSpawnEntry {

	private final ResourceLocation name;

	@Deprecated
	protected final EntityType<?> type;
	protected final int weight;
	protected final int minDay;
	protected final int maxDay;
	protected final int minSpawns;
	protected final int maxSpawns;
	protected CompoundNBT nbt = null;

	public HordeSpawnEntry(EntityType<?> type) {
		this(type, 0, 0, 0, 0, 0);
	}

	HordeSpawnEntry(EntityType<?> type, int weight, int minDay, int maxDay, int minSpawns, int maxSpawns) {
		this.name = ForgeRegistries.ENTITIES.getKey(type);
		this.type = type;
		this.weight = weight;
		this.minDay = minDay;
		this.maxDay = maxDay;
		this.minSpawns = minSpawns;
		this.maxSpawns = maxSpawns;
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

	@Deprecated
	public EntityType<?> getEntity() {
		return type;
	}

	public ResourceLocation getName() {
		return name;
	}

	public CompoundNBT getNBT() {
		return nbt == null ? new CompoundNBT() : nbt;
	}

	public void setNBT(CompoundNBT nbt) {
		this.nbt=nbt;
	}

	@Override
	public String toString() {
		String str = "HordeSpawnEntry[type=" + type + ",weight=" + weight + ",minDay=" + minDay + ",maxDay=" + maxDay + "]";
		return nbt == null ? str : str + "{" + nbt + "}";
	}

}
