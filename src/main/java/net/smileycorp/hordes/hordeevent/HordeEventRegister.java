package net.smileycorp.hordes.hordeevent;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.GameData;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.hordes.common.CommonUtils;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.Hordes;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class HordeEventRegister {

	protected static List<HordeSpawnEntry> spawnlist = new ArrayList<HordeSpawnEntry>();

	@SuppressWarnings("unchecked")
	public static void readConfig() {
		Hordes.logInfo("Trying to read spawn table from config");
		if (ConfigHandler.hordeSpawnList == null) {
			Hordes.logError("Error reading config.", new NullPointerException("Spawn table has loaded as null"));
		}
		else if (ConfigHandler.hordeSpawnList.length<=0) {
			Hordes.logError("Error reading config.", new Exception("Spawn table in config is empty"));
		}
		for (String name : ConfigHandler.hordeSpawnList) {
			try {
				Class<?> clazz = null;
				int weight=0;
				int minDay=0;
				int maxDay=0;
				NBTTagCompound nbt = null;
				//check if it matches the syntax for a registry name
				if (name.contains(":")) {
					String[] nameSplit = name.split("-");
					if (nameSplit.length>1) {
						if (nameSplit[0].contains("{")) {

							String nbtstring = nameSplit[0].substring(nameSplit[0].indexOf("{"));
							nameSplit[0] = nameSplit[0].substring(0, nameSplit[0].indexOf("{"));
							nbt = CommonUtils.parseNBT(name, nbtstring);
						}
						ResourceLocation loc = new ResourceLocation(nameSplit[0]);
						if (GameData.getEntityRegistry().containsKey(loc)) {
							clazz = (Class<? extends EntityLiving>) GameData.getEntityRegistry().getValue(loc).getEntityClass();
							try {
								weight = Integer.valueOf(nameSplit[1]);
							} catch (Exception e) {
								throw new Exception("Entity " + name + " has weight value " + nameSplit[1] + " which is not a valid integer");
							}
							try {
								minDay = Integer.valueOf(nameSplit[2]);
							} catch (Exception e) {
								throw new Exception("Entity " + name + " has min day value " + nameSplit[2] + " which is not a valid integer");
							}
							if (nameSplit.length>3) {
								try {
									maxDay = Integer.valueOf(nameSplit[3]);
								} catch (Exception e) {
									throw new Exception("Entity " + name + " has max day value " + nameSplit[3] + " which is not a valid integer");
								}
							}
						} else {
							throw new Exception("Entity " + name + " is not registered");
						}
					} else {
						throw new Exception("Entry " + name + " is not in the correct format");
					}
				}
				if (clazz == null) {
					throw new Exception("Entry " + name + " is not in the correct format");
				}
				if (EntityLiving.class.isAssignableFrom(clazz) && weight>0) {
					HordeSpawnEntry entry = new HordeSpawnEntry((Class<? extends EntityLiving>) clazz, weight, minDay, maxDay);
					if (nbt != null) {
						entry.setTagCompound(nbt);
					}
					spawnlist.add(entry);
					Hordes.logInfo("Loaded entity " + name + " as " + clazz.getName() + " with weight " + weight + ", min day " + minDay + " and max day " + maxDay);
				} else {
					throw new Exception("Entity " + name + " is not an instance of EntityLiving");
				}
			} catch (Exception e) {
				Hordes.logError("Error adding entity " + name + " " + e.getCause() + " " + e.getMessage(), e);
				continue;
			}
		}
	}


	public static WeightedOutputs<HordeSpawnEntry> getSpawnTable(int day) {
		List<Entry<HordeSpawnEntry, Integer>> spawnmap = new ArrayList<Entry<HordeSpawnEntry, Integer>>();
		for(HordeSpawnEntry entry : spawnlist) {
			if (entry.getMinDay() <= day && (entry.getMaxDay() == 0 || entry.getMaxDay() >= day)) {
				spawnmap.add(new SimpleEntry<HordeSpawnEntry, Integer>(entry, entry.getWeight()));
				Hordes.logInfo("Adding entry " + entry.toString() + " to hordespawn on day " + day);
			}
		}
		return new WeightedOutputs<HordeSpawnEntry>(1, spawnmap);
	}

	public static List<HordeSpawnEntry> getEntriesFor(EntityLiving entity) {
		return getEntriesFor(entity.getClass());
	}

	public static List<HordeSpawnEntry> getEntriesFor(Class<? extends EntityLiving> clazz) {
		List<HordeSpawnEntry> list = new ArrayList<HordeSpawnEntry>();
		for (HordeSpawnEntry entry : spawnlist) if (entry.getEntity() == clazz) list.add(entry);
		return list;
	}

	public static HordeSpawnEntry getEntryFor(EntityLiving entity, int day) {
		for (HordeSpawnEntry entry : getEntriesFor(entity)) {
			if (entry.getMinDay() <= day && (entry.getMaxDay() == 0 || entry.getMaxDay() >= day)) {
				return entry;
			}
		}
		return null;
	}

}
