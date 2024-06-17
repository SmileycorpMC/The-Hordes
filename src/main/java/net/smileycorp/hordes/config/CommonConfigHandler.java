package net.smileycorp.hordes.config;

import net.minecraftforge.common.config.Configuration;
import net.smileycorp.hordes.common.HordesLogger;

public class CommonConfigHandler {
	
	//misc
	public static boolean zombiesBurn;
	public static boolean skeletonsBurn;
	public static boolean zombieVillagersCanBeCured;
	public static boolean aggressiveZombieHorses;
	public static boolean zombieHorsesBurn;
	public static boolean skeletonHorsesBurn;
	public static boolean zombiesScareHorses;
	
	//load config properties
	public static void syncConfig(Configuration config) {
		HordesLogger.logInfo("Trying to load config");
		try{
			config.load();
			HordeEventConfig.syncConfig(config);
			InfectionConfig.syncConfig(config);
			ZombiePlayersConfig.syncConfig(config);
			zombiesBurn = config.get("Misc", "zombiesBurn", true, "Whether zombies burn in sunlight.").getBoolean();
			skeletonsBurn = config.get("Misc", "skeletonsBurn", true, "Whether skeletons burn in sunlight.").getBoolean();
			zombieVillagersCanBeCured = config.get("Misc", "zombieVillagersCanBeCured", false, "Whether zombie villagers have vanilla curing mechanics or not").getBoolean();
			aggressiveZombieHorses = config.get("Misc", "aggressiveZombieHorses", true, "Whether zombie horses are aggressive or not.").getBoolean();
			zombieHorsesBurn = config.get("Misc", "zombieHorsesBurn", true, "Whether zombie horses burn in sunlight").getBoolean();
			skeletonHorsesBurn = config.get("Misc", "skeletonHorsesBurn", true, "Whether skeleton horses burn in sunlight").getBoolean();
			zombiesScareHorses = config.get("Misc", "zombiesScareHorses", true, "Whether zombies scare horses").getBoolean();
		} catch(Exception e) {
		} finally {
			if (config.hasChanged()) config.save();
		}

	}

}
