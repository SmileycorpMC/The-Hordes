package net.smileycorp.hordes.common.hordeevent;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.Hordes;

public class HordeEventRegister {

	protected static Map<EntityType<?>, List<HordeSpawnEntry>> spawnlist = new HashMap<EntityType<?>, List<HordeSpawnEntry>>();
	private static boolean tested = false;

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
					EntityType<?> type = null;
					int weight=0;
					int minDay=0;
					int maxDay=0;
					CompoundNBT nbt = null;
					//check if it matches the syntax for a registry name
					if (name.contains(":")) {
						String[] nameSplit = name.split("-");
						if (nameSplit.length>1) {
							if (nameSplit[0].contains("{")) {

								String nbtstring = nameSplit[0].substring(nameSplit[0].indexOf("{"));
								nameSplit[0] = nameSplit[0].substring(0, nameSplit[0].indexOf("{"));
								try {
									CompoundNBT parsed = JsonToNBT.parseTag(nbtstring);
									if (parsed != null) nbt = parsed;
								} catch (Exception e) {
									Hordes.logError("Error parsing nbt for entity " + name + " " + e.getMessage(), e);
									//throw new Exception("NBT " + nbt + "is not valid json");
								}
							}
							ResourceLocation loc = new ResourceLocation(nameSplit[0]);
							if (ForgeRegistries.ENTITIES.containsKey(loc)) {
								type = ForgeRegistries.ENTITIES.getValue(loc);
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
					if (type == null) {
						throw new Exception("Entry " + name + " is not in the correct format");
					}
					HordeSpawnEntry entry = new HordeSpawnEntry(type, weight, minDay, maxDay);
					if (nbt != null) {
						entry.setTagCompound(nbt);
					}
					if (spawnlist.containsKey(type)) spawnlist.get(type).add(entry);
					else spawnlist.put(type, Arrays.asList(entry));
					Hordes.logInfo("Loaded entity " + name + " as " + type.toString() + " with weight " + weight + ", min day " + minDay + " and max day " + maxDay);
				} catch (Exception e) {
					Hordes.logError("Error adding entity " + name + " " + e.getCause() + " " + e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			Hordes.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
		}
	}

	public static WeightedOutputs<EntityType<?>> getSpawnTable(int day) {
		if (!tested) testEntries();
		List<Entry<EntityType<?>, Integer>> spawnmap = new ArrayList<Entry<EntityType<?>, Integer>>();
		for (Entry<EntityType<?>, List<HordeSpawnEntry>> mapentry : spawnlist.entrySet()) {
			for(HordeSpawnEntry entry : mapentry.getValue()) {
				if (entry.getMinDay() <= day && (entry.getMaxDay() == 0 || entry.getMaxDay() >= day)) {
					spawnmap.add(new SimpleEntry<EntityType<?>, Integer>(mapentry.getKey(), entry.getWeight()));
					Hordes.logInfo("Adding entry " + entry.toString() + " to hordespawn on day " + day);
				}
			}
		}
		return new WeightedOutputs<EntityType<?>>(1, spawnmap);
	}

	public static List<HordeSpawnEntry> getEntriesFor(MobEntity entity) {
		return getEntriesFor(entity.getType());
	}

	public static List<HordeSpawnEntry> getEntriesFor(EntityType<?> type) {
		if (!tested) testEntries();
		if (spawnlist.containsKey(type)) return spawnlist.get(type);
		return Arrays.asList();
	}

	public static HordeSpawnEntry getEntryFor(MobEntity entity, int day) {
		if (!tested) testEntries();
		for (HordeSpawnEntry entry : getEntriesFor(entity)) {
			if (entry.getMinDay() <= day && (entry.getMaxDay() == 0 || entry.getMaxDay() >= day)) {
				return entry;
			}
		}
		return null;
	}

	private static void testEntries() {
		List<EntityType<?>> toRemove = new ArrayList<EntityType<?>>();
		for (Entry<EntityType<?>, List<HordeSpawnEntry>> entry : spawnlist.entrySet()) {
			try {
				Entity entity = entry.getKey().create(ServerLifecycleHooks.getCurrentServer().overworld());
				if (!(entity instanceof MobEntity)) toRemove.add(entry.getKey());
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		for (EntityType<?> type : toRemove) spawnlist.remove(type);
		tested = true;
	}

}
