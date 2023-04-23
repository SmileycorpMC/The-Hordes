package net.smileycorp.hordes.common;

import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {
	static Configuration config;

	//horde event
	public static boolean enableHordeEvent;
	public static int hordeSpawnAmount;
	public static double hordeSpawnMultiplier;
	public static int hordeSpawnMax;
	public static int hordeSpawnDuration;
	public static int hordeSpawnInterval;
	public static int hordeStartTime;
	public static int hordeSpawnDays;
	public static int hordeSpawnVariation;
	public static int dayLength;
	public static boolean spawnFirstDay;
	public static boolean canSleepDuringHorde;
	public static int eventNotifyMode;
	public static int eventNotifyDuration;
	public static boolean hordeSpawnSound;
	public static double hordeMultiplayerScaling;
	public static boolean pauseEventServer;
	public static String[] hordeSpawnList;

	//infection
	public static boolean enableMobInfection;
	public static boolean infectVillagers;
	public static int villagerInfectChance;
	public static boolean infectPlayers;
	public static boolean infectSlowness;
	public static boolean infectHunger;
	public static int playerInfectChance;
	public static int ticksForEffectStage;
	public static boolean infectionSpawnsZombiePlayers;
	public static boolean playerInfectionVisuals;
	public static boolean playerInfectSound;
	public static String[] infectionEntities;
	public static String[] cureItemList;
	public static String[] infectionConversionList;
	public static boolean infectionEntitiesAggroConversions;
	public static double effectStageTickReduction;

	//misc
	public static boolean zombieGraves;
	public static boolean zombiePlayersFireImmune;
	public static boolean zombiePlayersBurn;
	public static boolean zombiePlayersOnlyHurtByPlayers;
	public static boolean zombiesBurn;
	public static boolean skeletonsBurn;
	public static boolean zombieVillagersCanBeCured;
	public static boolean aggressiveZombieHorses;
	public static boolean zombieHorsesBurn;
	public static boolean skeletonHorsesBurn;
	public static boolean zombiesScareHorses;

	//load config properties
	public static void syncConfig() {
		Hordes.logInfo("Trying to load config");
		try{
			config.load();
			//horde event
			enableHordeEvent = config.get("Horde Spawn Event", "enableHordeEvent", true, "Set to false to completely disable the horde event and anything relating to it.").getBoolean();
			hordeSpawnAmount = config.get("Horde Spawn Event", "spawnAmount", 25, "Amount of mobs to spawn per wave.").getInt();
			hordeSpawnMultiplier = config.get("Horde Spawn Event", "hordeSpawnMultiplier", 1.1, "Multiplier by which the spawn amount increases by each time the event naturally spawns. (Set to 1 to disable scaling.)").getDouble();
			hordeSpawnDuration = config.get("Horde Spawn Event", "hordeSpawnDuration", 6000, "Time in ticks the spawn lasts for.").getInt();
			hordeSpawnInterval = config.get("Horde Spawn Event", "hordeSpawnInterval", 1000, "Time in ticks between spawns for the horde spawn event.").getInt();
			hordeStartTime = config.get("Horde Spawn Event", "hordeStartTime", 18000, "What time of day does the horde event start? eg 18000 is midnight with default day length.").getInt();
			hordeSpawnDays = config.get("Horde Spawn Event", "hordeSpawnDays", 10, "Amount of days between horde spawns").getInt();
			hordeSpawnVariation = config.get("Horde Spawn Event", "hordeSpawnVariation", 0, "Amount of days a horde event can be randomly extended by").getInt();
			hordeSpawnMax = config.get("Horde Spawn Event", "hordeSpawnMax", 120, "Max cap for the number of entities that can exist from the horde at once.").getInt();
			dayLength = config.get("Horde Spawn Event", "dayLength", 24000, "Length of a day (use only if you have another day that changes the length of the day/night cycle) Default is 24000").getInt();
			spawnFirstDay = config.get("Horde Spawn Event", "spawnFirstDay", false, "Set to true to enable the horde spawning on the first day. (Game day 0)").getBoolean();
			canSleepDuringHorde = config.get("Horde Spawn Event", "canSleepDuringHorde", false, "Set to false to disable the use of beds during a horde event.").getBoolean();
			eventNotifyMode = config.get("Horde Spawn Event", "eventNotifyMode", 1, "How do players get notified of a horde event. 0: Off, 1: Chat, 2:Action Bar, 3:Title").getInt();
			eventNotifyDuration = config.get("Horde Spawn Event", "eventNotifyDuration", 60, "How long in ticks does the horde notification appear? (Only applies to modes 2 and 3)").getInt();
			hordeSpawnSound = config.get("Horde Spawn Event", "hordeSpawnSound", true, "Play a sound when a horde wave spawns.").getBoolean();
			hordeMultiplayerScaling = config.get("Horde Spawn Event", "hordeMultiplayerScaling", 0.8, "How much should the size of each horde scale down by when multiple players are near each other?").getDouble();
			pauseEventServer = config.get("Horde Spawn Event", "pauseEventServer", true, "Do the daylight cycle (and active horde events get paused while there are no players online.).").getBoolean();
			hordeSpawnList = config.get("Horde Spawn Event", "spawnList",
					new String[]{"minecraft:zombie-35-0-20", "minecraft:zombie_villager-1-0-10", "minecraft:husk-45-30-0", "minecraft:zombie_horse-3-30-0"},
					"A list of entities to spawn followed by the spawn weight then the day they first appear on then the last day. Higher weight is more common. Leave the last value to 0 to set no max day, entities can have nbt attached to them.(e.g. minecraft:zombie-20-0-0{ActiveEffects:[{Id:12,Amplifier:0,Duration:10000}]})").getStringList();

			//infection
			enableMobInfection = config.get("Infection", "enableMobInfection", true, "Set to false to completely disable mob infection and anything related to it.").getBoolean();
			infectVillagers = config.get("Infection", "infectVillagers", true, "Can villagers be infected.").getBoolean();
			villagerInfectChance = config.get("Infection", "villagerInfectChance", 85, "Chance out of 100 for a villager to get infected").getInt();
			infectPlayers = config.get("Infection", "infectPlayers", true, "Can players be infected.").getBoolean();
			infectSlowness = config.get("Infection", "infectSlowness", true, "Whether later levels of infected should slightly slow movement speed? ").getBoolean();
			infectHunger = config.get("Infection", "infectHunger", true, "Whether later levels of infected should depleet hunger quicker? ").getBoolean();
			playerInfectChance = config.get("Infection", "playerInfectChance", 75, "Chance out of 100 for a player to get infected").getInt();
			ticksForEffectStage = config.get("Infection", "ticksForEffectStage", 6000, "How long do each of the 4 effect phases last for before the next phase is activated?").getInt();
			infectionSpawnsZombiePlayers = config.get("Infection", "infectionSpawnsZombiePlayers", true, "Do players who die to infection spawn a zombie??").getBoolean();
			playerInfectionVisuals = config.get("Infection", "playerInfectionVisuals", true, "Tint the player's screen and display other visual effects if they are infected.").getBoolean();
			playerInfectSound = config.get("Infection", "playerInfectSound", true, "Play a sound when the player beomes infected.").getBoolean();
			infectionEntities = config.get("Infection", "infectionEntities",
					new String[]{"minecraft:zombie", "minecraft:zombie_villager", "minecraft:husk", "minecraft:zombie_horse", "hordes:zombie_player"},
					"Mobs which are based on entities in this list can cause the infection effect.").getStringList();
			cureItemList = config.get("Infection", "cureItemList",
					new String[]{"minecraft:golden_apple:*"},
					"A list of items which can cure infection when 'consumed' use '*' to specify any metadata, can accept nbt tags. eg.minecraft:golden_apple:*, minecraft:potion{Potion: \\\"minecraft:strong_regeneration\\\"}").getStringList();
			infectionConversionList = config.get("Infection", "infectionConversionList",
					new String[]{"minecraft:horse-65-minecraft:zombie_horse"},
					"A list of entities that can be infected, followed by the chance out of 100 to infect, then the entity to convert them to, " +
							"entities can have nbt attached to them.(e.g. minecraft:villager-85-minecraft:zombie_villager{ActiveEffects:[{Id:12,Amplifier:0,Duration:10000}]}), "
							+ "note: players and villagers have special code accociated with them, and should not be in this list").getStringList();
			infectionEntitiesAggroConversions = config.get("Infection", "infectionEntitiesAggroConversions", true, "Do entities on the infectionEntities list automatically target entities on the infectionConversionList").getBoolean();
			effectStageTickReduction = config.get("Infection", "effectStageTickReduction", 0.95, "What factor should the infection potion effect timer be multiplied by for each cured infection? (Resets on death, set to 1 to disable scaling)").getDouble();

			//misc
			zombieGraves = config.get("Misc", "zombieGraves", false, "Whether to use zombie players as graves all the time. (Even if infection is disabled)").getBoolean();
			zombiePlayersFireImmune = config.get("Misc", "zombiePlayersFireImmune", false, "Whether zombie players should be immune to fire damage").getBoolean();
			zombiePlayersBurn = config.get("Misc", "zombiePlayersBurn", false, "Whether zombie players burn in sunlight.").getBoolean();
			zombiePlayersOnlyHurtByPlayers = config.get("Misc", "zombiePlayersOnlyHurtByPlayers", false, "Whether zombie players are immune to all damage from non player sources.").getBoolean();
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
