package net.smileycorp.hordes.common.infection;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.CommonUtils;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.infection.capability.IInfection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InfectionRegister {

	private static Map<EntityType<?>, InfectionConversionEntry> conversionTable = new HashMap<>();

	public static void readConfig() {
		readEntityConversions();
	}

	@SuppressWarnings("unchecked")
	private static void readEntityConversions() {
		Hordes.logInfo("Trying to read conversion table from config");
		if (CommonConfigHandler.infectionConversionList == null) {
			Hordes.logError("Error reading config.", new NullPointerException("Conversion table has loaded as null"));
		}
		else if (CommonConfigHandler.infectionConversionList.get().size()<=0) {
			Hordes.logError("Error reading config.", new Exception("Conversion table in config is empty"));
		}
		for (String name : CommonConfigHandler.infectionConversionList.get()) {
			try {
				EntityType<?> type = null;
				int infectChance = 0;
				EntityType<?> result = null;
				CompoundTag nbt = null;
				String[] nameSplit = name.split("-");
				if (nameSplit.length >= 3) {
					try {
						if (nameSplit[0].contains("{")) nameSplit[0] = nameSplit[0].substring(0, nameSplit[0].indexOf("{"));
						ResourceLocation entity = new ResourceLocation(nameSplit[0]);
						if (!ForgeRegistries.ENTITIES.containsKey(entity)) throw new Exception("Entity " + entity + " is not registered");
						type = ForgeRegistries.ENTITIES.getValue(entity);
					} catch (Exception e) {
						throw new Exception(nameSplit[0] + " is not a resourcelocation");
					}
					try {
						infectChance = Integer.valueOf(nameSplit[1]);
					} catch (Exception e) {
						throw new Exception(nameSplit[1] + " is not an integer");
					}
					if (nameSplit[2].contains("{")) {
						String nbtstring = nameSplit[2].substring(nameSplit[2].indexOf("{"));
						nameSplit[2] = nameSplit[2].substring(0, nameSplit[2].indexOf("{"));
						nbt = CommonUtils.parseNBT(nameSplit[2], nbtstring);
					}
					try {
						ResourceLocation entity = new ResourceLocation(nameSplit[2]);
						if (!ForgeRegistries.ENTITIES.containsKey(entity)) throw new Exception("Entity " + entity + " is not registered");
						result = ForgeRegistries.ENTITIES.getValue(entity);
					} catch (Exception e) {
						throw new Exception(nameSplit[2] + " is not a resourcelocation");
					}

				}
				if (type == null) {
					throw new Exception("Entry " + name + " is not in the correct format");
				}
				InfectionConversionEntry entry = new InfectionConversionEntry(infectChance, (EntityType<? extends LivingEntity>) result);
				if (nbt != null) {
					entry.setNBT(nbt);
				}
				conversionTable.put(type, entry);
				Hordes.logInfo("Loaded conversion " + name + " as " + type.toString() + " with infection chance " + infectChance + ", and converts to " + result.toString());
			} catch (Exception e) {
				Hordes.logError("Error adding conversion " + name + " " + e.getCause() + " " + e.getMessage(), e);
			}
		}
	}

	public static List<ItemStack> getCureList() {
		return ForgeRegistries.ITEMS.tags().getTag(HordesInfection.INFECTION_CURES_TAG)
				.stream().map(item->new ItemStack(item)).collect(Collectors.toList());
	}

	public static boolean isCure(ItemStack stack) {
		return stack.m_204117_(HordesInfection.INFECTION_CURES_TAG);
	}

	public static boolean canCauseInfection(Entity entity) {
		return entity instanceof Mob &&  canCauseInfection(entity.getType());
	}

	public static boolean canCauseInfection(EntityType<?> entity) {
		return entity.m_204039_(HordesInfection.INFECTION_ENTITIES_TAG);
	}

	public static boolean canBeInfected(Entity entity) {
		if (!(entity instanceof LivingEntity)) return false;
		return conversionTable.containsKey(entity.getType());
	}

	public static void tryToInfect(LivingEntity entity) {
		int c = entity.level.random.nextInt(100);
		if (c <= conversionTable.get(entity.getType()).getInfectChance()) {
			entity.addEffect(new MobEffectInstance(HordesInfection.INFECTED.get(), getInfectionTime(entity), 0));
		}
	}

	public static void convertEntity(LivingEntity entity) {
		conversionTable.get(entity.getType()).convertEntity(entity);
	}

	public static int getInfectionTime(LivingEntity entity) {
		int time = CommonConfigHandler.ticksForEffectStage.get();
		LazyOptional<IInfection> optional = entity.getCapability(Hordes.INFECTION);
		if (optional.isPresent()) time = (int)((double)time * Math.pow(CommonConfigHandler.effectStageTickReduction.get(), optional.resolve().get().getInfectionCount()));
		return time;
	}

}
