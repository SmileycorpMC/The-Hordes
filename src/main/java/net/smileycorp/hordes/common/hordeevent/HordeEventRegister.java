package net.smileycorp.hordes.common.hordeevent;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
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
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Hordes;

public class HordeEventRegister {

	protected static List<HordeSpawnEntry> spawnlist = new ArrayList<HordeSpawnEntry>();
	private static boolean tested = false;

	public static void readConfig() {
		Hordes.logInfo("Trying to read spawn table from config");
		if (CommonConfigHandler.hordeSpawnList == null) {
			Hordes.logError("Error reading config.", new NullPointerException("Spawn table has loaded as null"));
		}
		else if (CommonConfigHandler.hordeSpawnList.get().size()<=0) {
			Hordes.logError("Error reading config.", new Exception("Spawn table in config is empty"));
		}
		for (String name : CommonConfigHandler.hordeSpawnList.get()) {
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
							nbt = parseNBT(name, nbtstring);
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
					entry.setNBT(nbt);
				}
				spawnlist.add(entry);
				Hordes.logInfo("Loaded entity " + name + " as " + type.toString() + " with weight " + weight + ", min day " + minDay + " and max day " + maxDay);
			} catch (Exception e) {
				Hordes.logError("Error adding entity " + name + " " + e.getCause() + " " + e.getMessage(), e);
			}
		}
	}

	private static CompoundNBT parseNBT(String name, String nbtstring) {
		CompoundNBT nbt = null;
		try {
			CompoundNBT parsed = JsonToNBT.parseTag(nbtstring);
			if (parsed != null) nbt = parsed;
			else throw new NullPointerException("Parsed NBT is null.");
		} catch (Exception e) {
			Hordes.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
			Hordes.logError("Error parsing nbt for entity " + name + " " + e.getMessage(), e);
		}
		return nbt;
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

	public static List<HordeSpawnEntry> getEntriesFor(MobEntity entity) {
		return getEntriesFor(entity.getType());
	}

	public static List<HordeSpawnEntry> getEntriesFor(EntityType<?> type) {
		List<HordeSpawnEntry> list = new ArrayList<HordeSpawnEntry>();
		for (HordeSpawnEntry entry : spawnlist) if (entry.getEntity() == type) list.add(entry);
		return list;
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
		List<HordeSpawnEntry> toRemove = new ArrayList<HordeSpawnEntry>();
		for (HordeSpawnEntry entry : spawnlist) {
			try {
				Entity entity = entry.getEntity().create(ServerLifecycleHooks.getCurrentServer().overworld());
				if (!(entity instanceof MobEntity)) toRemove.add(entry);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		for (HordeSpawnEntry type : toRemove) spawnlist.remove(type);
		tested = true;
	}

}
