package net.smileycorp.hordes.hordeevent;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.util.RandomSource;
import net.smileycorp.atlas.api.util.WeightedOutputs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WeightedSpawnTable extends WeightedOutputs<HordeSpawnEntry> {
    
    private Map<HordeSpawnEntry, Integer> timesSpawned = Maps.newHashMap();
    
    public WeightedSpawnTable(List<Entry<HordeSpawnEntry, Integer>> spawnmap) {
        super(1, spawnmap);
    }
    
    public List<HordeSpawnEntry> getResults(RandomSource rand, int tries) {
        List<HordeSpawnEntry> list = Lists.newArrayList();
        List<Entry<HordeSpawnEntry, Integer>> mappedEntries = Lists.newArrayList();
        int maxWeight = 0;
        int max = 0;
        System.out.println(tries);
        for(Entry<HordeSpawnEntry, Integer> entry : entries) {
            HordeSpawnEntry spawnEntry = entry.getKey();
            if (spawnEntry.maxSpawns <= 0) max = -1;
            else if (max > -1) max += spawnEntry.maxSpawns;
            int spawned = 0;
            for (int i = 0; i < spawnEntry.minSpawns; i++) {
                list.add(spawnEntry);
                tries--;
                spawned++;
            }
            timesSpawned.put(spawnEntry, spawned);
            mappedEntries.add(new SimpleEntry<>(spawnEntry, maxWeight));
            maxWeight += entry.getValue();
        }
        if (max > -1) tries = max - list.size();
        if (tries > 0 && maxWeight > 0) {
            Collections.reverse(mappedEntries);
            for (int i = 0; i < tries; i++) {
                HordeSpawnEntry spawnEntry = getEntry(rand, mappedEntries, maxWeight);
                if (spawnEntry != null) list.add(spawnEntry);
            }
        }
        return list;
    }
    
    public HordeSpawnEntry getEntry(RandomSource rand, List<Entry<HordeSpawnEntry, Integer>> mappedEntries, int max) {
        int result = rand.nextInt(max);
        for (Entry<HordeSpawnEntry, Integer> entry : mappedEntries) if (result >= entry.getValue()) {
            HordeSpawnEntry spawnEntry = entry.getKey();
            if (spawnEntry.maxSpawns > 0 && spawnEntry.maxSpawns <= timesSpawned.get(spawnEntry)) return getEntry(rand, mappedEntries, max);
            timesSpawned.put(spawnEntry, timesSpawned.get(spawnEntry) + 1);
            return entry.getKey();
        }
        return null;
    }
    
}
