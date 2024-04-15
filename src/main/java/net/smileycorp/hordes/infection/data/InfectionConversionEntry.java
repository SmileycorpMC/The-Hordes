package net.smileycorp.hordes.infection.data;

import com.google.gson.JsonObject;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataRegistry;

public class InfectionConversionEntry {

	protected final EntityType<? extends MobEntity> entity, result;
	protected final float infectChance;

	protected final CompoundNBT nbt;

	private InfectionConversionEntry(EntityType<? extends MobEntity>entity, EntityType<? extends MobEntity> result, float infectChance, CompoundNBT nbt) {
		if (entity == null || result == null) throw new NullPointerException();
		this.entity = entity;
		this.result = result;
		this.infectChance = infectChance;
		this.nbt = nbt;
		HordesLogger.logInfo("Loaded conversion " + entity + " to " + result + (nbt != null ? nbt : "") + " with chance of " + infectChance);
	}

	public LivingEntity convertEntity(MobEntity entity) {
		LivingConversionEvent.Pre preEvent = new LivingConversionEvent.Pre(entity, result, i -> {});
		MinecraftForge.EVENT_BUS.post(preEvent);
		LivingEntity zombie = entity.convertTo(result, true);
		if (zombie instanceof AgeableEntity) ((AgeableEntity) zombie).setAge(entity.isBaby() ? -1000000 : 0);
		if (zombie instanceof ZombieEntity) ((ZombieEntity) zombie).setBaby(entity.isBaby());
		if (nbt != null) zombie.readAdditionalSaveData(nbt);
		LivingConversionEvent.Post postEvent = new LivingConversionEvent.Post(entity, zombie);
		MinecraftForge.EVENT_BUS.post(postEvent);
		return zombie;
	}

	public EntityType<?> getEntity() {
		return entity;
	}

	public boolean shouldInfect(LivingEntity entity) {
		return entity.getRandom().nextFloat() <= InfectionDataLoader.INSTANCE.getModifiedInfectChance(entity, infectChance);
	}
	
	public static InfectionConversionEntry deserialize(JsonObject json) throws Exception {
		EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(json.get("entity").getAsString()));
		EntityType<?> converts_to = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(json.get("converts_to").getAsString()));
		float chance = json.get("chance").getAsFloat();
		CompoundNBT nbt = json.has("nbt") ? DataRegistry.parseNBT(entity.toString(), json.get("nbt").getAsString()) : null;
		return new InfectionConversionEntry((EntityType<? extends MobEntity>)entity, (EntityType<? extends MobEntity>)converts_to, chance, nbt);
	}

}
