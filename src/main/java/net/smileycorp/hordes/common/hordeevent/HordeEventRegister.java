package net.smileycorp.hordes.common.hordeevent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.GameData;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.TheHordes;

public class HordeEventRegister {
	
	protected static List<HordeSpawnEntry> spawnlist = new ArrayList<HordeSpawnEntry>();
	
	public static void init() {
		TheHordes.logInfo("Trying to read spawn table from config");
		try {
			if (ConfigHandler.hordeSpawnList == null) {
				throw new Exception("Spawn table has loaded as null");
			}
			else if (ConfigHandler.hordeSpawnList.length<=0) {
				throw new Exception("Spawn table in config is empty");
			}
			
			for (String name : ConfigHandler.hordeSpawnList) {
				//if we haven't already got all entity names stored get them to check against
				try {
					Class clazz = null;
					int weight=0;
					int minDay=0;
					int maxDay=0;
					//check if it matches they syntax for a registry name
					if (name.contains(":")) {
						String[] nameSplit = name.split("-");
						if (nameSplit.length>1) {
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
						spawnlist.add(new HordeSpawnEntry(clazz, weight, minDay, maxDay));
						TheHordes.logInfo("Loaded entity " + name + " as " + clazz.getName() + " with weight " + weight + ", min day " + minDay + " and max day " + maxDay);
					} else {
						throw new Exception("Entity " + name + " is not an instance of EntityLiving");
					}
				} catch (Exception e) {
					TheHordes.logError("Error adding entity " + name + " " + e.getCause() + " " + e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			TheHordes.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
		}
	}

	public static WeightedOutputs getSpawnTable(int day) {
		Map<Class<? extends EntityLiving>, Integer> spawnmap = new HashMap<Class<? extends EntityLiving>, Integer>();
		for (HordeSpawnEntry entry : HordeEventRegister.spawnlist) {
			if (entry.getMinDay() <= day && (entry.getMaxDay() == 0 || entry.getMaxDay() >= day)) {
				spawnmap.put(entry.getEntityClass(), entry.getWeight());
				TheHordes.logInfo("Adding entry " + entry.toString() + " to hordespawn on day " + day);
			}
		}
		return new WeightedOutputs(spawnmap);
	}
	
}
