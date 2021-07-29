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
	public static int playerInfectChance;
	public static int ticksForEffectStage;
	public static String[] infectionEntities;
	public static String[] cureItemList;
	
	//load config properties
	public static void syncConfig() {
		TheHordes.logInfo("Trying to load config");
		try{
			config.load();
			//horde event
			enableHordeEvent = config.get("Horde Spawn Event", "enableHordeEvent", true, "Set to false to completely disable the horde event and anything relating to it.").getBoolean();
			hordeSpawnAmount = config.get("Horde Spawn Event", "spawnAmount", 25, "Amount of mobs to spawn per wave.").getInt();
			hordeSpawnMultiplier = config.get("Horde Spawn Event", "hordeSpawnMultiplier", 1.1, "Multiplier by which the spawn amount increases by each time the event naturally spawns. (Set to 1 to disable scaling.)").getDouble();
			hordeSpawnDuration = config.get("Horde Spawn Event", "hordeSpawnDuration", 6000, "Time in ticks the spawn lasts for.").getInt();
			hordeSpawnInterval = config.get("Horde Spawn Event", "hordeSpawnInterval", 1000, "Time in ticks between spawns for the horde spawn event.").getInt();
			hordeStartTime = config.get("Horde Spawn Event", "hordeStartTime", 18000, "What time of day does the horde event start? eg 18000 is midnight.").getInt();
			hordeSpawnDays = config.get("Horde Spawn Event", "hordeSpawnDays", 10, "Amount of days between horde spawns").getInt();
			hordeSpawnVariation = config.get("Horde Spawn Event", "hordeSpawnVariation", 0, "Amount of days a horde event can be randomly extended by").getInt();
			hordeSpawnMax = config.get("Horde Spawn Event", "hordeSpawnMax", 120, "Max cap for the number of entities that can exist from the horde at once.").getInt();
			spawnFirstDay = config.get("Horde Spawn Event", "spawnFirstDay", false, "Set to true to enable the horde spawning on the first day.").getBoolean();
			canSleepDuringHorde = config.get("Horde Spawn Event", "canSleepDuringHorde", false, "Set to false to disable the use of beds during a horde event.").getBoolean();
			eventNotifyMode = config.get("Horde Spawn Event", "eventNotifyMode", 1, "How do players get notified of a horde event. 0: Off, 1: Chat, 2:Action Bar, 3:Title").getInt();
			eventNotifyDuration = config.get("Horde Spawn Event", "eventNotifyDuration", 60, "How long in ticks does the horde notification appear? (Only applies to modes 2 and 3)").getInt();
			hordeSpawnSound = config.get("Horde Spawn Event", "hordeSpawnSound", true, "Play a sound when a horde wave spawns.").getBoolean();
			hordeMultiplayerScaling = config.get("Horde Spawn Event", "hordeMultiplayerScaling", 0.8, "How much should the size of each horde scale down by when multiple players are near each other?").getDouble();
			pauseEventServer = config.get("Horde Spawn Event", "pauseEventServer", true, "Do the daylight cycle (and active horde events get paused while there are no players online.).").getBoolean();
			hordeSpawnList = config.get("Horde Spawn Event", "spawnList",
					new String[]{"minecraft:zombie-20-0-20", "minecraft:zombie_villager-5-0-10", "minecraft:husk-20-30-0"}, 
					"A list of entities to spawn followed by the spawn weight then the day they first appear on then the last day. Higher weight is more common. Leave the last value to 0 to set no max day, entities can have nbt attached to them.(e.g. minecraft:zombie-20-0-0{ActiveEffects:[{Id:12,Amplifier:0,Duration:10000}]})").getStringList();
			//infection
			enableMobInfection = config.get("Horde Spawn Event", "enableMobInfection", true, "Set to false to completely disable mob infection and anything related to it.").getBoolean();
			infectVillagers = config.get("Infection", "infectVillagers", true, "Can villagers be infected.").getBoolean();
			villagerInfectChance = config.get("Infection", "villagerInfectChance", 85, "Chance out of 100 for a villager to get infected").getInt();
			infectPlayers = config.get("Infection", "infectPlayers", true, "Can players be infected.").getBoolean();
			playerInfectChance = config.get("Infection", "playerInfectChance", 75, "Chance out of 100 for a player to get infected").getInt();
			ticksForEffectStage = config.get("Infection", "ticksForEffectStage", 6000, "How long do each of the 4 effect phases last for before the next phase is activated?").getInt();
			infectionEntities = config.get("Infection", "infectionEntities",
					new String[]{"minecraft:zombie"}, 
					"Mobs which are based on entities in this list can cause the infection effect.").getStringList();
			cureItemList = config.get("Infection", "cureItemList",
					new String[]{"minecraft:golden_apple:*"}, 
					"A list of items which can cure infection when 'consumed' use '*' to specify any metadata, can accept nbt tags. eg.minecraft:golden_apple:*, minecraft:potion{Potion: \"minecraft:strong_regeneration\"}").getStringList();
		} catch(Exception e) {
		} finally {
	    	if (config.hasChanged()) config.save();
		}
		
	}

}
