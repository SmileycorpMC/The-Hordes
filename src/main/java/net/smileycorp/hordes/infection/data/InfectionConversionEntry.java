package net.smileycorp.hordes.infection.data;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.atlas.api.util.Func;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataRegistry;

public class InfectionConversionEntry {

	protected final EntityType<? extends Mob> entity, result;
	protected final float infectChance;

	protected final CompoundTag nbt;

	private InfectionConversionEntry(EntityType<? extends Mob>entity, EntityType<? extends Mob> result, float infectChance, CompoundTag nbt) {
		if (entity == null || result == null) throw new NullPointerException();
		this.entity = entity;
		this.result = result;
		this.infectChance = infectChance;
		this.nbt = nbt;
		HordesLogger.logInfo("Loaded conversion " + entity + " to " + result + (nbt != null ? nbt : "") + " with chance of " + infectChance);
	}

	public LivingEntity convertEntity(Mob entity) {
		LivingConversionEvent.Pre preEvent = new LivingConversionEvent.Pre(entity, result, Func::Void);
		MinecraftForge.EVENT_BUS.post(preEvent);
		LivingEntity zombie = entity.convertTo(result, true);
		if (zombie instanceof AgeableMob) ((AgeableMob) zombie).setAge(entity.isBaby() ? -1000000 : 0);
		if (zombie instanceof Zombie) ((Zombie) zombie).setBaby(entity.isBaby());
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
		EntityType<?> entity = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(json.get("entity").getAsString()));
		EntityType<?> converts_to = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(json.get("converts_to").getAsString()));
		float chance = json.get("chance").getAsFloat();
		CompoundTag nbt = json.has("nbt") ? DataRegistry.parseNBT(entity.toShortString(), json.get("nbt").getAsString()) : null;
		return new InfectionConversionEntry((EntityType<? extends Mob>)entity, (EntityType<? extends Mob>)converts_to, chance, nbt);
	}

}
