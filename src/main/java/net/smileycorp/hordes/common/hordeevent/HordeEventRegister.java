package net.smileycorp.hordes.common.hordeevent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.GameData;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.Hordes;

public class HordeEventRegister {
	
	protected static Map<Class<? extends EntityLiving>, HordeSpawnEntry> spawnlist = new HashMap<Class<? extends EntityLiving>, HordeSpawnEntry>();
	
	public static void readConfig() {
		Hordes.logInfo("Trying to read spawn table from config");
		try {
			if (ConfigHandler.hordeSpawnList == null) {
				throw new Exception("Spawn table has loaded as null");
			}
			else if (ConfigHandler.hordeSpawnList.length<=0) {
				throw new Exception("Spawn table in config is empty");
			}
			for (String name : ConfigHandler.hordeSpawnList) {
				try {
					Class clazz = null;
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
								try {
									NBTTagCompound parsed = JsonToNBT.getTagFromJson(nbtstring);
									if (parsed != null) nbt = parsed;
								} catch (Exception e) {
									Hordes.logError("Error parsing nbt for entity " + name + " " + e.getMessage(), e);
									//throw new Exception("NBT " + nbt + "is not valid json");
								}
							}
							ResourceLocation loc = new ResourceLocation(nameSplit[0]);
							if (GameData.getEntityRegistry().containsKey(loc)) {
								clazz = GameData.getEntityRegistry().getValue(loc).getEntityClass();
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
						HordeSpawnEntry entry = new HordeSpawnEntry(clazz, weight, minDay, maxDay);
						if (nbt != null) {
							entry.setTagCompound(nbt);
						}
						spawnlist.put(clazz, entry);
						Hordes.logInfo("Loaded entity " + name + " as " + clazz.getName() + " with weight " + weight + ", min day " + minDay + " and max day " + maxDay);
					} else {
						throw new Exception("Entity " + name + " is not an instance of EntityLiving");
					}
				} catch (Exception e) {
					Hordes.logError("Error adding entity " + name + " " + e.getCause() + " " + e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			Hordes.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
		}
	}

	public static WeightedOutputs getSpawnTable(int day) {
		Map<Class<? extends EntityLiving>, Integer> spawnmap = new HashMap<Class<? extends EntityLiving>, Integer>();
		for (Entry<Class<? extends EntityLiving>, HordeSpawnEntry> mapentry : spawnlist.entrySet()) {
			HordeSpawnEntry entry = mapentry.getValue();
			if (entry.getMinDay() <= day && (entry.getMaxDay() == 0 || entry.getMaxDay() >= day)) {
				spawnmap.put(mapentry.getKey(), entry.getWeight());
				Hordes.logInfo("Adding entry " + entry.toString() + " to hordespawn on day " + day);
			}
		}
		return new WeightedOutputs(spawnmap);
	}
	
	public static HordeSpawnEntry getEntryFor(EntityLiving entity) {
		return getEntryFor(entity.getClass());
	}
	
	public static HordeSpawnEntry getEntryFor(Class<? extends EntityLiving> clazz) {
		if (spawnlist.containsKey(clazz)) return spawnlist.get(clazz);
		return null;
	}
	
}
