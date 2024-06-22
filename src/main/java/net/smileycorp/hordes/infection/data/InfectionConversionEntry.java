package net.smileycorp.hordes.infection.data;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
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
		NeoForge.EVENT_BUS.post(preEvent);
		LivingEntity zombie = entity.convertTo(result, true);
		if (zombie instanceof AgeableMob) ((AgeableMob) zombie).setAge(entity.isBaby() ? -1000000 : 0);
		if (zombie instanceof Zombie) ((Zombie) zombie).setBaby(entity.isBaby());
		if (nbt != null) zombie.readAdditionalSaveData(nbt);
		LivingConversionEvent.Post postEvent = new LivingConversionEvent.Post(entity, zombie);
		NeoForge.EVENT_BUS.post(postEvent);
		return zombie;
	}

	public EntityType<?> getEntity() {
		return entity;
	}

	public boolean shouldInfect(LivingEntity entity) {
		return entity.getRandom().nextFloat() <= InfectionDataLoader.INSTANCE.getModifiedInfectChance(entity, infectChance);
	}
	
	public static InfectionConversionEntry deserialize(JsonObject json) throws Exception {
		EntityType<?> entity = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(json.get("entity").getAsString()));
		EntityType<?> converts_to = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(json.get("converts_to").getAsString()));
		float chance = json.get("chance").getAsFloat();
		CompoundTag nbt = json.has("nbt") ? DataRegistry.parseNBT(entity.toShortString(), json.get("nbt").getAsString()) : null;
		return new InfectionConversionEntry((EntityType<? extends Mob>)entity, (EntityType<? extends Mob>)converts_to, chance, nbt);
	}

}
