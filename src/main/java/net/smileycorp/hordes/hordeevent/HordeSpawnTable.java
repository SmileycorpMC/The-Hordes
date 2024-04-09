package net.smileycorp.hordes.hordeevent;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.util.WeightedOutputs;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataRegistry;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HordeSpawnTable {

    protected final List<HordeSpawnEntry> spawns;
    private final ResourceLocation name;

    private boolean tested;

   protected HordeSpawnTable(ResourceLocation name, List<HordeSpawnEntry> spawns) {
    this.name = name;
    this.spawns = spawns;
    }

    public ResourceLocation getName() {
       return name;
    }

    public WeightedOutputs<HordeSpawnEntry> getSpawnTable(int day) {
        List<Map.Entry<HordeSpawnEntry, Integer>> spawnmap = new ArrayList<>();
        for(HordeSpawnEntry entry : spawns) {
            if (entry.getMinDay() <= day && (entry.getMaxDay() == 0 || entry.getMaxDay() >= day)) {
                spawnmap.add(new AbstractMap.SimpleEntry<>(entry, entry.getWeight()));
                HordesLogger.logInfo("Adding entry " + entry + " to hordespawn on day " + day);
            }
        }
        return new WeightedSpawnTable(spawnmap);
    }

    public List<HordeSpawnEntry> getEntriesFor(Mob entity) {
        return getEntriesFor(entity.getType());
    }

    public List<HordeSpawnEntry> getEntriesFor(EntityType<?> type) {
        List<HordeSpawnEntry> list = new ArrayList<>();
        for (HordeSpawnEntry entry : spawns) if (entry.getEntity() == type) list.add(entry);
        return list;
    }

    public HordeSpawnEntry getEntryFor(Mob entity, int day) {
        if (!tested) testEntries();
        for (HordeSpawnEntry entry : getEntriesFor(entity)) {
            if (entry.getMinDay() <= day && (entry.getMaxDay() == 0 || entry.getMaxDay() >= day)) {
                return entry;
            }
        }
        return null;
    }

    private void testEntries() {
        List<HordeSpawnEntry> toRemove = new ArrayList<>();
        for (HordeSpawnEntry entry : spawns) {
            try {
                Entity entity = entry.getEntity().create(ServerLifecycleHooks.getCurrentServer().overworld());
                if (!(entity instanceof Mob)) toRemove.add(entry);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        for (HordeSpawnEntry type : toRemove) spawns.remove(type);
        tested = true;
    }

    public static HordeSpawnTable deserialize(ResourceLocation name, JsonElement json) throws Exception {
        List<HordeSpawnEntry> spawns = Lists.newArrayList();
        for (JsonElement element : json.getAsJsonArray()) {
            String entity = null;
            try {
                EntityType<?> type = null;
                int weight = 0;
                int minDay = 0;
                int maxDay = 0;
                int minSpawns = 0;
                int maxSpawns = 0;
                CompoundTag nbt = null;
                if (element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    entity = obj.get("entity").getAsString();
                    type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entity));
                    if (obj.has("weight")) weight = obj.get("weight").getAsInt();
                    if (obj.has("first_day")) minDay = obj.get("first_day").getAsInt();
                    if (obj.has("last_day")) maxDay = obj.get("last_day").getAsInt();
                    if (obj.has("min_spawns")) minSpawns = obj.get("min_spawns").getAsInt();
                    if (obj.has("max_spawns")) maxSpawns = obj.get("max_spawns").getAsInt();
                    if (obj.has("nbt")) nbt = DataRegistry.parseNBT(entity, obj.get("nbt").getAsString());
                } else {
                    //check if it matches the syntax for a registry name
                    String data = element.getAsString();
                    if (data.contains(":")) {
                        String[] dataSplit = data.split("-");
                        if (dataSplit.length > 1) {
                            if (dataSplit[0].contains("{")) {
                                String nbtstring = dataSplit[0].substring(dataSplit[0].indexOf("{"));
                                dataSplit[0] = dataSplit[0].substring(0, dataSplit[0].indexOf("{"));
                                nbt = DataRegistry.parseNBT(data, nbtstring);
                            }
                            entity = dataSplit[0];
                            ResourceLocation loc = new ResourceLocation(dataSplit[0]);
                            if (ForgeRegistries.ENTITY_TYPES.containsKey(loc)) {
                                type = ForgeRegistries.ENTITY_TYPES.getValue(loc);
                                try {
                                    weight = Integer.valueOf(dataSplit[1]);
                                } catch (Exception e) {
                                    throw new Exception("Entity " + name + " has weight value " + dataSplit[1] + " which is not a valid integer");
                                }
                                try {
                                    minDay = Integer.valueOf(dataSplit[2]);
                                } catch (Exception e) {
                                    throw new Exception("Entity " + name + " has min day value " + dataSplit[2] + " which is not a valid integer");
                                }
                                if (dataSplit.length > 3) {
                                    try {
                                        maxDay = Integer.valueOf(dataSplit[3]);
                                    } catch (Exception e) {
                                        throw new Exception("Entity " + name + " has max day value " + dataSplit[3] + " which is not a valid integer");
                                    }
                                }
                            } else throw new Exception("Entity " + name + " is not registered");
                        } else throw new Exception("Entry " + name + " is not in the correct format");
                    }
                    if (type == null) throw new Exception("Entry " + name + " is not in the correct format");
                    HordeSpawnEntry entry = new HordeSpawnEntry(type, weight, minDay, maxDay, minSpawns, maxSpawns);
                    if (nbt != null) entry.setNBT(nbt);
                }
                HordesLogger.logInfo("Loaded entity " + entity + " as " + type.toString() + " with weight " + weight + ", min day " + minDay + " and max day " + maxDay);
                HordeSpawnEntry entry = new HordeSpawnEntry(type, weight, minDay, maxDay, minSpawns, maxSpawns);
                if (nbt != null) entry.setNBT(nbt);
                spawns.add(entry);
            } catch (Exception e) {
                HordesLogger.logError("Error adding entity " + entity + " " + e.getCause() + " " + e.getMessage(), e);
            }
        }
       return new HordeSpawnTable(name, spawns);
    }

}
