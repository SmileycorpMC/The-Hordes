package net.smileycorp.hordes.common.hordeevent;

import net.minecraft.entity.EntityLiving;

public class HordeSpawnEntry {
	
	protected final Class<?  extends EntityLiving> clazz;
	protected final int weight;
	protected final int minDay;
	protected final int maxDay;
	
	public HordeSpawnEntry(Class<?  extends EntityLiving> clazz, int weight, int minDay, int maxDay) {
		this.clazz=clazz;
		this.weight=weight;
		this.minDay=minDay;
		this.maxDay=maxDay;
	}
	
	public Class<?  extends EntityLiving> getEntityClass() {
		return clazz;
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
		return "HordeSpawnEntry[clazz="+clazz+",weight="+weight+",minDay="+minDay+",maxDay="+maxDay+"]";
	}
	
}
