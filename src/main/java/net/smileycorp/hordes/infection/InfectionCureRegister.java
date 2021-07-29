package net.smileycorp.hordes.infection;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.smileycorp.atlas.api.util.RecipeUtils;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.TheHordes;

public class InfectionCureRegister {
	
	private static List<ItemStack> cures = new ArrayList<ItemStack>();
	private static List<ItemStack> curesClient = new ArrayList<ItemStack>();
	
	public static void readConfig() {
		try {
			if (ConfigHandler.hordeSpawnList == null) {
				throw new Exception("Cure list has loaded as null");
			}
			else if (ConfigHandler.hordeSpawnList.length<=0) {
				throw new Exception("Cure list in config is empty");
			}
			cures = readData(ConfigHandler.cureItemList);
		} catch (Exception e) {
			TheHordes.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
		}
	}
	
	public static void readPacketData(String data) {
		try {
			String[] splitData = data.split(";");
			curesClient = readData(splitData);
		} catch (Exception e) {
			TheHordes.logError("Failed to read data from server, " + e.getCause() + " " + e.getMessage(), e);
		}
	}
	
	public static String getPacketData() {
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
	
	public static List<ItemStack> readData(String[] data) throws Exception {
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
					TheHordes.logError("Error parsing nbt for entity " + name + " " + e.getMessage(), e);
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
					TheHordes.logError("Entry" + name + " has a non integer, non wildcard metadata value", e); 
				}
				if (ForgeRegistries.ITEMS.containsKey(loc)) {
					ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(loc), 1, meta);
					if (nbt!=null) {
						stack.setTagCompound(nbt);
					}
					stacks.add(stack);
				}
			} else {
				throw new Exception();
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
}
