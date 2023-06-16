package net.smileycorp.hordes.common.hordeevent;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class MutableSpawnTable extends HordeSpawnTable {

   private MutableSpawnTable(ResourceLocation name, List<HordeSpawnEntry> spawns) {
        super(name, spawns);
   }

    public static MutableSpawnTable of(HordeSpawnTable table) {
       return new MutableSpawnTable(table.getName(), table.spawns);
    }

    public List<HordeSpawnEntry> getSpawns() {
       return spawns;
    }

}
