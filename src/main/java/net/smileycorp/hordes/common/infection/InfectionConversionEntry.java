package net.smileycorp.hordes.common.infection;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingConversionEvent;

public class InfectionConversionEntry {

	protected final int infectChance;
	protected final EntityType<? extends LivingEntity> result;

	protected CompoundNBT nbt = null;

	protected InfectionConversionEntry(int infectChance, EntityType<? extends LivingEntity> result) {
		this.infectChance = infectChance;
		this.result = result;
	}

	public LivingEntity convertEntity(LivingEntity entity) {
		LivingConversionEvent.Pre preEvent = new LivingConversionEvent.Pre(entity, result, (i)->{});
		MinecraftForge.EVENT_BUS.post(preEvent);
		World world = entity.level;
		LivingEntity zombie = preEvent.getOutcome().create(world);
		zombie.setPos(entity.getX(), entity.getY(), entity.getZ());
		for (EquipmentSlotType slot : EquipmentSlotType.values()) {
			zombie.setItemSlot(slot, entity.getItemBySlot(slot));
		}
		if (entity.hasCustomName()) {
			zombie.setCustomName(entity.getCustomName());
		}
		if (zombie instanceof AgeableEntity) ((AgeableEntity) zombie).setAge(entity.isBaby() ? -1000000 : 0);
		if (zombie instanceof ZombieEntity) ((ZombieEntity) zombie).setBaby(entity.isBaby());
		if (nbt != null) entity.readAdditionalSaveData(nbt);
		LivingConversionEvent.Post postEvent = new LivingConversionEvent.Post(entity, zombie);
		MinecraftForge.EVENT_BUS.post(postEvent);
		zombie = postEvent.getOutcome();
		world.addFreshEntity(zombie);
		entity.kill();
		return zombie;
	}

	public int getInfectChance() {
		return infectChance;
	}

	public void setNBT(CompoundNBT nbt) {
		this.nbt = nbt;
	}

}
