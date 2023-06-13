package net.smileycorp.hordes.common.hordeevent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.hordeevent.data.HordeTableLoader;

import java.util.Random;

public class HordeEventRegister {

	public static HordeSpawnTable getSpawnTable(Level level, Player player, Random rand) {
		return HordeTableLoader.INSTANCE.getDefaultTable();
	}

}
