package net.smileycorp.hordes.config.data.infection;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.config.data.DataRegistry;

public class InfectionConversionEntry {

	protected final EntityEntry entity, result;
	protected final float infectChance;

	protected final NBTTagCompound nbt;

	private InfectionConversionEntry(EntityEntry entity, EntityEntry result, float infectChance, NBTTagCompound nbt) {
		if (entity == null || result == null) throw new NullPointerException();
		this.entity = entity;
		this.result = result;
		this.infectChance = infectChance;
		this.nbt = nbt;
		HordesLogger.logInfo("Loaded conversion " + entity.getEntityClass() + " to " + result.getEntityClass() + (nbt != null ? nbt : "") + " with chance of " + infectChance);
	}

	public EntityLiving convertEntity(EntityLivingBase entity) {
		EntityLiving zombie = (EntityLiving) result.newInstance(entity.getEntityWorld());
		if (zombie instanceof EntityAgeable) ((EntityAgeable) zombie).setGrowingAge(entity.isChild() ? -1000000 : 0);
		if (zombie instanceof EntityZombie) ((EntityZombie) zombie).setChild(entity.isChild());
		if (nbt != null) zombie.readFromNBT(nbt);
		return zombie;
	}

	public EntityEntry getEntity() {
		return entity;
	}

	public boolean shouldInfect(EntityLivingBase entity) {
		return entity.getRNG().nextFloat() <= InfectionDataLoader.INSTANCE.getModifiedInfectChance(entity, infectChance);
	}
	
	public static InfectionConversionEntry deserialize(JsonObject json) throws Exception {
		EntityEntry entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(json.get("entity").getAsString()));
		EntityEntry converts_to = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(json.get("converts_to").getAsString()));
		float chance = json.get("chance").getAsFloat();
		NBTTagCompound nbt = json.has("nbt") ? DataRegistry.parseNBT(entity.toString(), json.get("nbt").getAsString()) : null;
		return new InfectionConversionEntry(entity, converts_to, chance, nbt);
	}

}
