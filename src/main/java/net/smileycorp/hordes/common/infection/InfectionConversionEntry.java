package net.smileycorp.hordes.common.infection;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingConversionEvent;

public class InfectionConversionEntry {

	protected final int infectChance;
	protected final EntityType<? extends Mob> result;

	protected CompoundTag nbt = null;

	protected InfectionConversionEntry(int infectChance, EntityType<? extends Mob> result) {
		this.infectChance = infectChance;
		this.result = result;
	}

	public LivingEntity convertEntity(Mob entity) {
		LivingConversionEvent.Pre preEvent = new LivingConversionEvent.Pre(entity, result, (i)->{});
		MinecraftForge.EVENT_BUS.post(preEvent);
		LivingEntity zombie = entity.convertTo((EntityType)preEvent.getOutcome(), false);
		if (zombie instanceof AgeableMob) ((AgeableMob) zombie).setAge(entity.isBaby() ? -1000000 : 0);
		if (zombie instanceof Zombie) ((Zombie) zombie).setBaby(entity.isBaby());
		if (nbt != null) entity.readAdditionalSaveData(nbt);
		LivingConversionEvent.Post postEvent = new LivingConversionEvent.Post(entity, zombie);
		MinecraftForge.EVENT_BUS.post(postEvent);
		zombie = postEvent.getOutcome();
		return zombie;
	}

	public int getInfectChance() {
		return infectChance;
	}

	public void setNBT(CompoundTag nbt) {
		this.nbt = nbt;
	}

}
