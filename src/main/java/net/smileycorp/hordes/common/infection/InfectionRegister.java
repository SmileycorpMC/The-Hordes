package net.smileycorp.hordes.common.infection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.atlas.api.util.RecipeUtils;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.CommonUtils;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.integration.jei.JEIPluginInfection;

public class InfectionRegister {

	private static List<ItemStack> cures = new ArrayList<>();
	private static List<ItemStack> curesClient = new ArrayList<>();

	private static List<EntityType<?>> infectionEntities = new ArrayList<>();

	private static Map<EntityType<?>, InfectionConversionEntry> conversionTable = new HashMap<>();

	public static void readConfig() {
		readInfectionEntities();
		readCureItems();
		readEntityConversions();
	}

	private static void readInfectionEntities() {
		try {
			if (CommonConfigHandler.infectionEntities == null) {
				throw new Exception("Infection entity list has loaded as null");
			}
			else if (CommonConfigHandler.infectionEntities.get().size() <= 0) {
				throw new Exception("Infection entity list in config is empty");
			}
			for (String name : CommonConfigHandler.infectionEntities.get()) {
				String[] nameSplit = name.split(":");
				if (nameSplit.length>=2) {
					ResourceLocation loc = new ResourceLocation(nameSplit[0], nameSplit[1]);
					if (ForgeRegistries.ENTITIES.containsKey(loc)) {
						EntityType<?> type = ForgeRegistries.ENTITIES.getValue(loc);
						if (type != null) {
							infectionEntities.add(type);
						}
					}
				} else {
					throw new Exception(name + " is not a valid registry.");
				}
			}
		} catch (Exception e) {
			Hordes.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
		}
	}

	private static void readCureItems() {
		try {
			if (CommonConfigHandler.cureItemList == null) {
				throw new Exception("Cure list has loaded as null");
			}
			else if (CommonConfigHandler.cureItemList.get().size()<=0) {
				throw new Exception("Cure list in config is empty");
			}
			cures = parseCureData(CommonConfigHandler.cureItemList.get());
		} catch (Exception e) {
			Hordes.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
		}
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

	public static void readCurePacketData(String data) {
		try {
			curesClient = parseCureData(Lists.newArrayList(data.split(";")));
		} catch (Exception e) {
			Hordes.logError("Failed to read data from server, " + e.getCause() + " " + e.getMessage(), e);
		}
		if (ModList.get().isLoaded("jei")) JEIPluginInfection.setRecipes(curesClient);
	}

	public static String getCurePacketData() {
		StringBuilder builder = new StringBuilder();
		for (ItemStack stack : cures) {
			builder.append(stack.getItem().getRegistryName());
			if (stack.getTag() != null) {
				builder.append(stack.getTag().toString());
			}
			builder.append(";");
		}
		return builder.toString();
	}

	public static List<ItemStack> parseCureData(List<String> data) throws Exception {
		List<ItemStack> stacks = new ArrayList<>();
		for (String name : data) {
			CompoundTag nbt = null;
			if (name.contains("{")) {
				String nbtstring = name.substring(name.indexOf("{"));
				name = name.substring(0, name.indexOf("{"));
				try {
					CompoundTag parsed = TagParser.parseTag(nbtstring);
					if (parsed != null) nbt = parsed;
				} catch (Exception e) {
					Hordes.logError("Error parsing nbt for entity " + name + " " + e.getMessage(), e);
				}
			}
			String[] nameSplit = name.split(":");
			if (nameSplit.length>=2) {
				ResourceLocation loc = new ResourceLocation(nameSplit[0], nameSplit[1]);
				if (ForgeRegistries.ITEMS.containsKey(loc)) {
					ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(loc));
					if (nbt!=null) {
						stack.setTag(nbt);
					}
					stacks.add(stack);
				}
			} else {
				throw new Exception(name + " is not a valid registry");
			}
		}
		return stacks;
	}

	static List<ItemStack> getCureList() {
		List<ItemStack> result = new ArrayList<>();
		for (ItemStack stack : Thread.currentThread().getThreadGroup() == SidedThreadGroups.CLIENT ? curesClient : cures) {
			result.add(new ItemStack(stack.getItem()));
		}
		return result;
	}

	public static void addCureItem(ItemStack stack) {
		cures.add(stack);
	}

	public static void removeCureItem(ItemStack stack) {
		for (ItemStack match : cures) {
			if (RecipeUtils.compareItemStacks(match, stack, true)) {
				cures.remove(match);
			}
		}
	}

	public static boolean isCure(ItemStack stack) {
		for (ItemStack cure : (Thread.currentThread().getThreadGroup() == SidedThreadGroups.CLIENT) ? curesClient : cures) {
			if (RecipeUtils.compareItemStacks(stack, cure, cure.getTag() != null)) {
				return true;
			}
		}
		return false;
	}

	public static boolean canCauseInfection(Entity entity) {
		if (entity instanceof Mob) {
			for (EntityType<?> type : infectionEntities) {
				if (entity.getType() == type) return true;
			}
		}
		return false;
	}

	public static boolean canBeInfected(Entity entity) {
		if (!(entity instanceof LivingEntity)) return false;
		return conversionTable.containsKey(entity.getType());
	}

	public static void tryToInfect(LivingEntity entity) {
		int c = entity.level.random.nextInt(100);
		if (c <= conversionTable.get(entity.getType()).getInfectChance()) {
			entity.addEffect(new MobEffectInstance(HordesInfection.INFECTED.get(), CommonConfigHandler.ticksForEffectStage.get(), 0));
		}
	}

	public static void convertEntity(LivingEntity entity) {
		conversionTable.get(entity.getType()).convertEntity(entity);
	}

}
