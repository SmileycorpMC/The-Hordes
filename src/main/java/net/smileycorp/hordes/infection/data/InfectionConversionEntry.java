package net.smileycorp.hordes.infection.data;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.smileycorp.atlas.api.util.Func;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.common.mixinutils.VillageMerchant;

public class InfectionConversionEntry {

	protected final EntityType<? extends Mob> entity, result;
	protected final float protection;

	protected final CompoundTag nbt;

	private InfectionConversionEntry(EntityType<? extends Mob>entity, EntityType<? extends Mob> result, float protection, CompoundTag nbt) {
		if (entity == null || result == null) throw new NullPointerException();
		this.entity = entity;
		this.result = result;
		this.protection = protection;
		this.nbt = nbt;
		HordesLogger.logInfo("Loaded conversion " + entity + " to " + result + (nbt != null ? nbt : "") + " with an infection resistance of " + protection);
	}

	public LivingEntity convertEntity(Mob entity) {
		LivingConversionEvent.Pre preEvent = new LivingConversionEvent.Pre(entity, result, Func::Void);
		NeoForge.EVENT_BUS.post(preEvent);
		LivingEntity zombie = entity.convertTo(result, true);
		if (zombie instanceof AgeableMob) ((AgeableMob) zombie).setAge(entity.isBaby() ? -1000000 : 0);
		if (zombie instanceof Zombie) ((Zombie) zombie).setBaby(entity.isBaby());
		if (nbt != null) zombie.readAdditionalSaveData(nbt);
		if (entity instanceof VillagerDataHolder && zombie instanceof VillagerDataHolder)
			((VillagerDataHolder)zombie).setVillagerData(((VillagerDataHolder)entity).getVillagerData());
		if (entity instanceof VillageMerchant && zombie instanceof VillageMerchant) {
			((VillageMerchant)zombie).setMerchantGossips(((VillageMerchant)entity).getMerchantGossips());
			((VillageMerchant)zombie).setMerchantOffers(((VillageMerchant)entity).getMerchantOffers());
			((VillageMerchant)zombie).setMerchantXp(((VillageMerchant)entity).getMerchantXp());
		}
		if (entity instanceof Zombie) ((Zombie)zombie).finalizeSpawn((ServerLevel) entity.level(),
				entity.level().getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.CONVERSION,
				new Zombie.ZombieGroupData(false, true));
		LivingConversionEvent.Post postEvent = new LivingConversionEvent.Post(entity, zombie);
		NeoForge.EVENT_BUS.post(postEvent);
		return zombie;
	}

	public EntityType<?> getEntity() {
		return entity;
	}

	public boolean shouldInfect(LivingEntity entity, LivingEntity attacker) {
		return entity.getRandom().nextFloat() <= InfectionData.INSTANCE.getInfectionChance(entity, attacker);
	}
	
	public static InfectionConversionEntry deserialize(JsonObject json) throws Exception {
		EntityType<?> entity = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(json.get("entity").getAsString()));
		EntityType<?> converts_to = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(json.get("converts_to").getAsString()));
		float chance = json.get("protection").getAsFloat();
		CompoundTag nbt = json.has("nbt") ? DataRegistry.parseNBT(entity.toShortString(), json.get("nbt").getAsString()) : null;
		return new InfectionConversionEntry((EntityType<? extends Mob>)entity, (EntityType<? extends Mob>)converts_to, chance, nbt);
	}

}
