package net.smileycorp.hordes.infection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.smileycorp.atlas.api.util.RecipeUtils;
import net.smileycorp.hordes.common.CommonUtils;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.infection.capability.IInfection;
import net.smileycorp.hordes.integration.jei.JEIPluginInfection;

public class InfectionRegister {

	private static List<ItemStack> cures = new ArrayList<>();
	private static List<ItemStack> curesClient = new ArrayList<>();

	private static List<Class<? extends EntityLivingBase>> infectionEntities =  Lists.newArrayList();
	private static Map<Class<? extends EntityLivingBase>, InfectionConversionEntry> conversionTable = Maps.newHashMap();

	public static void readConfig() {
		readInfectionEntities();
		readCureItems();
		readEntityConversions();
	}

	@SuppressWarnings("unchecked")
	private static void readInfectionEntities() {
		try {
			if (ConfigHandler.infectionEntities == null) {
				throw new Exception("Infection entity list has loaded as null");
			}
			else if (ConfigHandler.infectionEntities.length <= 0) {
				throw new Exception("Infection entity list in config is empty");
			}
			for (String name : ConfigHandler.infectionEntities) {
				String[] nameSplit = name.split(":");
				if (nameSplit.length>=2) {
					ResourceLocation loc = new ResourceLocation(nameSplit[0], nameSplit[1]);
					if (ForgeRegistries.ENTITIES.containsKey(loc)) {
						EntityEntry entry = ForgeRegistries.ENTITIES.getValue(loc);
						Class<?> clazz = entry.getEntityClass();
						if (EntityLivingBase.class.isAssignableFrom(clazz)) {
							infectionEntities.add((Class<? extends EntityLivingBase>) clazz);
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
			if (ConfigHandler.cureItemList == null) {
				throw new Exception("Cure list has loaded as null");
			}
			else if (ConfigHandler.cureItemList.length<=0) {
				throw new Exception("Cure list in config is empty");
			}
			cures = parseCureData(ConfigHandler.cureItemList);
		} catch (Exception e) {
			Hordes.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static void readEntityConversions() {
		Hordes.logInfo("Trying to read conversion table from config");
		if (ConfigHandler.infectionConversionList == null) {
			Hordes.logError("Error reading config.", new NullPointerException("Conversion table has loaded as null"));
		}
		else if (ConfigHandler.infectionConversionList.length<=0) {
			Hordes.logError("Error reading config.", new Exception("Conversion table in config is empty"));
		}
		for (String name : ConfigHandler.infectionConversionList) {
			try {
				Class<?> clazz = null;
				int infectChance = 0;
				Class<?> result = null;
				NBTTagCompound nbt = null;
				String[] nameSplit = name.split("-");
				if (nameSplit.length >= 3) {
					try {
						if (nameSplit[0].contains("{")) nameSplit[0] = nameSplit[0].substring(0, nameSplit[0].indexOf("{"));
						ResourceLocation entity = new ResourceLocation(nameSplit[0]);
						if (!ForgeRegistries.ENTITIES.containsKey(entity)) throw new Exception("Entity " + entity + " is not registered");
						clazz = ForgeRegistries.ENTITIES.getValue(entity).getEntityClass();
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
						result = ForgeRegistries.ENTITIES.getValue(entity).getEntityClass();
					} catch (Exception e) {
						throw new Exception(nameSplit[2] + " is not a resourcelocation");
					}

				}
				if (!(EntityLivingBase.class.isAssignableFrom(clazz) && EntityLivingBase.class.isAssignableFrom(result))) {
					throw new Exception("Entry " + name + " is not in the correct format");
				}
				InfectionConversionEntry entry = new InfectionConversionEntry(infectChance, (Class<? extends EntityLivingBase>) result);
				if (nbt != null) {
					entry.setNBT(nbt);
				}
				conversionTable.put((Class<? extends EntityLivingBase>) clazz, entry);
				Hordes.logInfo("Loaded conversion " + name + " as " + clazz.toString() + " with infection chance " + infectChance + ", and converts to " + result.toString());
			} catch (Exception e) {
				Hordes.logError("Error adding conversion " + name + " " + e.getCause() + " " + e.getMessage(), e);
			}
		}
	}

	public static void readCurePacketData(String data) {
		try {
			String[] splitData = data.split(";");
			curesClient = parseCureData(splitData);
		} catch (Exception e) {
			Hordes.logError("Failed to read data from server, " + e.getCause() + " " + e.getMessage(), e);
		}
		if (Loader.isModLoaded("jei")) JEIPluginInfection.setRecipes(curesClient);
	}

	public static String getCurePacketData() {
		StringBuilder builder = new StringBuilder();
		for (ItemStack stack : cures) {
			builder.append(stack.getItem().getRegistryName());
			builder.append(":");
			builder.append(stack.getMetadata());
			if (stack.getTagCompound() != null) {
				builder.append(stack.getTagCompound().toString());
			}
			builder.append(";");
		}
		return builder.toString();
	}

	public static List<ItemStack> parseCureData(String[] data) throws Exception {
		List<ItemStack> stacks = new ArrayList<>();
		for (String name : data) {
			NBTTagCompound nbt = null;
			if (name.contains("{")) {
				String nbtstring = name.substring(name.indexOf("{"));
				name = name.substring(0, name.indexOf("{"));
				try {
					NBTTagCompound parsed = JsonToNBT.getTagFromJson(nbtstring);
					if (parsed != null) nbt = parsed;
				} catch (Exception e) {
					Hordes.logError("Error parsing nbt for entity " + name + " " + e.getMessage(), e);
				}
			}
			String[] nameSplit = name.split(":");
			if (nameSplit.length>=2) {
				ResourceLocation loc = new ResourceLocation(nameSplit[0], nameSplit[1]);
				int meta;
				try {
					meta = nameSplit.length > 2 ? (nameSplit[2].equals("*") ? OreDictionary.WILDCARD_VALUE : Integer.valueOf(nameSplit[2])) : 0;
				} catch (Exception e) {
					meta = 0;
					Hordes.logError("Entry" + name + " has a non integer, non wildcard metadata value", e);
				}
				if (ForgeRegistries.ITEMS.containsKey(loc)) {
					ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(loc), 1, meta);
					if (nbt!=null) {
						stack.setTagCompound(nbt);
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
		for (ItemStack stack : FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT ? curesClient : cures) {
			result.add(new ItemStack(stack.getItem(), stack.getMetadata()));
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
		for (ItemStack cure : (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) ? curesClient : cures) {
			if (RecipeUtils.compareItemStacks(stack, cure, cure.getTagCompound() != null)) {
				return true;
			}
		}
		return false;
	}

	public static boolean canCauseInfection(Entity entity) {
		if (entity instanceof EntityLivingBase) {
			for (Class<?> clazz : infectionEntities) {
				if (clazz.isAssignableFrom(entity.getClass())) return true;
			}
		}
		return false;
	}

	public static boolean canBeInfected(Entity entity) {
		if (!(entity instanceof EntityLivingBase)) return false;
		return conversionTable.containsKey(entity.getClass());
	}


	public static void tryToInfect(EntityLivingBase entity) {
		int c = entity.world.rand.nextInt(100);
		if (c <= conversionTable.get(entity.getClass()).getInfectChance()) {
			entity.addPotionEffect(new PotionEffect(HordesInfection.INFECTED, getInfectionTime(entity), 0));
		}
	}

	public static void convertEntity(EntityLivingBase entity) {
		conversionTable.get(entity.getClass()).convertEntity(entity);
	}

	public static int getInfectionTime(EntityLivingBase entity) {
		int time = ConfigHandler.ticksForEffectStage;
		IInfection cap = entity.getCapability(Hordes.INFECTION, null);
		if (cap != null) time = (int)(time * Math.pow(ConfigHandler.effectStageTickReduction, cap.getInfectionCount()));
		return time;
	}

}
