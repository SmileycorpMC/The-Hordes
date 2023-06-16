package net.smileycorp.hordes.common.infection;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.hordes.common.data.DataRegistry;

public class InfectionConversionEntry {

	protected final EntityType<? extends LivingEntity> entity, result;
	protected final float infectChance;

	protected final CompoundTag nbt;

	private InfectionConversionEntry(EntityType<? extends LivingEntity>entity, EntityType<? extends LivingEntity> result, int infectChance, CompoundTag nbt) {
		if (entity == null || result == null) throw new NullPointerException();
		this.entity = entity;
		this.result = result;
		this.infectChance = infectChance;
		this.nbt = nbt;
	}

	public LivingEntity convertEntity(LivingEntity entity) {
		LivingConversionEvent.Pre preEvent = new LivingConversionEvent.Pre(entity, result, (i)->{});
		MinecraftForge.EVENT_BUS.post(preEvent);
		Level level = entity.level();
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

	public EntityType<?> getEntity() {
		return entity;
	}

	public boolean shouldInfect(LivingEntity entity) {
		return (entity.level().random.nextFloat() <= infectChance);
	}

	public static InfectionConversionEntry deserialize(JsonObject json) throws Exception {
		EntityType<?> entity = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(json.get("entity").getAsString()));
		EntityType<?> converts_to = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(json.get("converts_to").getAsString()));
		int chance = 100;
		CompoundTag nbt = json.has("nbt") ? DataRegistry.parseNBT(entity.toShortString(), json.get("nbt").getAsString()) : new CompoundTag();
		return new InfectionConversionEntry((EntityType<? extends LivingEntity>)entity, (EntityType<? extends LivingEntity>)converts_to, chance, nbt);
	}

}
