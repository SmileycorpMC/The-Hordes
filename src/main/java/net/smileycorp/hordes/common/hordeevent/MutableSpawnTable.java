package net.smileycorp.hordes.common.hordeevent;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.kinds.IdF;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.hordes.common.CommonUtils;
import net.smileycorp.hordes.common.Hordes;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
