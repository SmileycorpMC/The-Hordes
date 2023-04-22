package net.smileycorp.hordes.infection;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.Hordes;

public class InfectionConversionEntry {

	protected final int infectChance;
	protected final Class<? extends EntityLivingBase> result;

	protected NBTTagCompound nbt = null;

	protected InfectionConversionEntry(int infectChance, Class<? extends EntityLivingBase> result) {
		this.infectChance = infectChance;
		this.result = result;
	}

	public EntityLivingBase convertEntity(EntityLivingBase entity) {
		World world = entity.world;
		try {
			EntityLivingBase zombie = result.getConstructor(World.class).newInstance(world);
			zombie.setPosition(entity.posX, entity.posY, entity.posZ);
			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
				zombie.setItemStackToSlot(slot, entity.getItemStackFromSlot(slot));
			}
			if (entity.hasCustomName()) {
				zombie.setCustomNameTag(entity.getCustomNameTag());
			}
			if (zombie instanceof EntityAgeable) ((EntityAgeable) zombie).setGrowingAge(entity.isChild() ? -1000000 : 0);
			if (zombie instanceof EntityZombie) ((EntityZombie) zombie).setChild(entity.isChild());
			if (nbt != null) entity.readFromNBT(nbt);
			world.spawnEntity(zombie);
			world.removeEntity(entity);
			return zombie;
		} catch (Exception e) {
			Hordes.logError("Failed to convert entity " + entity, e);
		}
		return null;
	}

	public int getInfectChance() {
		return infectChance;
	}

	public void setNBT(NBTTagCompound nbt) {
		this.nbt = nbt;
	}

}
