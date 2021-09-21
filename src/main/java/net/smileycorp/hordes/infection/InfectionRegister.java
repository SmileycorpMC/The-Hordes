package net.smileycorp.hordes.infection;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.smileycorp.atlas.api.util.RecipeUtils;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.Hordes;

public class InfectionRegister {
	
	private static List<ItemStack> cures = new ArrayList<ItemStack>();
	private static List<ItemStack> curesClient = new ArrayList<ItemStack>();
	
	private static List<Class<? extends EntityLiving>> infectionEntities = new ArrayList<Class<? extends EntityLiving>>();
	
	public static void readConfig() {
		readInfectionEntities();
		readCureItems();
	}
	
	private static void readInfectionEntities() {
		try {
			if (ConfigHandler.infectionEntities == null) {
				throw new Exception("Infection entity list has loaded as null");
			}
			else if (ConfigHandler.cureItemList.length<=0) {
				throw new Exception("Infection entity list in config is empty");
			}
			for (String name : ConfigHandler.infectionEntities) {
				String[] nameSplit = name.split(":");
				if (nameSplit.length>=2) {
					ResourceLocation loc = new ResourceLocation(nameSplit[0], nameSplit[1]);
					if (ForgeRegistries.ENTITIES.containsKey(loc)) {
						EntityEntry entry = ForgeRegistries.ENTITIES.getValue(loc);
						Class clazz = entry.getEntityClass();
						if (EntityLiving.class.isAssignableFrom(clazz)) {
							infectionEntities.add(clazz);
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

	public static void readCurePacketData(String data) {
		try {
			String[] splitData = data.split(";");
			curesClient = parseCureData(splitData);
		} catch (Exception e) {
			Hordes.logError("Failed to read data from server, " + e.getCause() + " " + e.getMessage(), e);
		}
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
		List<ItemStack> stacks = new ArrayList<ItemStack>();
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
		List<ItemStack> result = new ArrayList<ItemStack>();
		for (ItemStack stack : cures) {
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
		if (entity instanceof EntityLiving) {
			for (Class clazz : infectionEntities) {
				if (clazz.isAssignableFrom(entity.getClass())) return true;
			}
		}
		return false;
	}
}
