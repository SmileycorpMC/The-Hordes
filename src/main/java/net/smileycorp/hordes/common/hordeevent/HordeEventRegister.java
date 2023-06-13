package net.smileycorp.hordes.common.hordeevent;

import com.google.common.collect.Lists;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.data.HordeTableLoader;
import net.smileycorp.hordes.common.hordeevent.data.functions.HordeFunction;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class HordeEventRegister {

	public static WeightedOutputs<HordeSpawnEntry> getSpawnTable(int day) {
		return HordeTableLoader.INSTANCE.getDefaultTable().getSpawnTable(day);
	}

	public static List<HordeSpawnEntry> getEntriesFor(Mob entity) {
		return getEntriesFor(entity.getType());
	}

	public static List<HordeSpawnEntry> getEntriesFor(EntityType<?> type) {
		return  HordeTableLoader.INSTANCE.getDefaultTable().getEntriesFor(type);
	}

	public static HordeSpawnEntry getEntryFor(Mob entity, int day) {
		return HordeTableLoader.INSTANCE.getDefaultTable().getEntryFor(entity, day);
	}

}
