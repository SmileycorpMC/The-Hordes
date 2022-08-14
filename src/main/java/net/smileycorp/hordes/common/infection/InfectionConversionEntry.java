package net.smileycorp.hordes.common.infection;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingConversionEvent;

public class InfectionConversionEntry {

	protected final int infectChance;
	protected final EntityType<? extends LivingEntity> result;

	protected CompoundTag nbt = null;

	protected InfectionConversionEntry(int infectChance, EntityType<? extends LivingEntity> result) {
		this.infectChance = infectChance;
		this.result = result;
	}

	public LivingEntity convertEntity(LivingEntity entity) {
		LivingConversionEvent.Pre preEvent = new LivingConversionEvent.Pre(entity, result, (i)->{});
		MinecraftForge.EVENT_BUS.post(preEvent);
		Level level = entity.level;
		LivingEntity zombie = preEvent.getOutcome().create(level);
		zombie.setPos(entity.getX(), entity.getY(), entity.getZ());
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			zombie.setItemSlot(slot, entity.getItemBySlot(slot));
		}
		if (entity.hasCustomName()) {
			zombie.setCustomName(entity.getCustomName());
		}
		if (zombie instanceof AgeableMob) ((AgeableMob) zombie).setAge(entity.isBaby() ? -1000000 : 0);
		if (zombie instanceof Zombie) ((Zombie) zombie).setBaby(entity.isBaby());
		if (nbt != null) entity.readAdditionalSaveData(nbt);
		LivingConversionEvent.Post postEvent = new LivingConversionEvent.Post(entity, zombie);
		MinecraftForge.EVENT_BUS.post(postEvent);
		zombie = postEvent.getOutcome();
		level.addFreshEntity(zombie);
		entity.kill();
		return zombie;
	}

	public int getInfectChance() {
		return infectChance;
	}

	public void setNBT(CompoundTag nbt) {
		this.nbt = nbt;
	}

}
