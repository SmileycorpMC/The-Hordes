package net.smileycorp.hordes.hordeevent;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class HordeSpawnEntry {
	
	private final ResourceLocation name;
	
	protected final EntityEntry type;
	protected final int weight;
	protected final int minDay;
	protected final int maxDay;
	protected final int minSpawns;
	protected final int maxSpawns;
	protected NBTTagCompound nbt = null;
	
	public HordeSpawnEntry(EntityEntry type) {
		this(type, 0, 0, 0, 0, 0);
	}
	
	HordeSpawnEntry(EntityEntry type, int weight, int minDay, int maxDay, int minSpawns, int maxSpawns) {
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

	public EntityEntry getEntity() {
		return type;
	}
	
	public ResourceLocation getName() {
		return name;
	}

	public NBTTagCompound getNBT() {
		return nbt == null ? new NBTTagCompound() : nbt;
	}

	public HordeSpawnEntry setNBT(NBTTagCompound nbt) {
		this.nbt = nbt;
		return this;
	}
	
	@Override
	public String toString() {
		String str = "HordeSpawnEntry[type=" + type + ",weight=" + weight + ",minDay=" + minDay + ",maxDay=" + maxDay + "]";
		return nbt == null ? str : str + "{" + nbt + "}";
	}

}
