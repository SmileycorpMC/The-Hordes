package net.smileycorp.hordes.common.infection;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.atlas.api.util.RecipeUtils;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.infection.jei.JEIPluginInfection;

import com.google.common.collect.Lists;

public class InfectionRegister {

	private static List<ItemStack> cures = new ArrayList<ItemStack>();
	private static List<ItemStack> curesClient = new ArrayList<ItemStack>();

	private static List<EntityType<?>> infectionEntities = new ArrayList<EntityType<?>>();

	public static void readConfig() {
		readInfectionEntities();
		readCureItems();
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
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		for (String name : data) {
			CompoundNBT nbt = null;
			if (name.contains("{")) {
				String nbtstring = name.substring(name.indexOf("{"));
				name = name.substring(0, name.indexOf("{"));
				try {
					CompoundNBT parsed = JsonToNBT.parseTag(nbtstring);
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
		List<ItemStack> result = new ArrayList<ItemStack>();
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
		if (entity instanceof MobEntity) {
			for (EntityType<?> type : infectionEntities) {
				if (entity.getType() == type) return true;
			}
		}
		return false;
	}
}
