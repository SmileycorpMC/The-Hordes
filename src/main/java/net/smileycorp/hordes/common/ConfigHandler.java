package net.smileycorp.hordes.common;

import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {
	static Configuration config;
	
	//horde event
	public static int hordeSpawnAmount;
	public static double hordeSpawnMultiplier;
	public static int hordeSpawnMax;
	public static int hordeSpawnDuration;
	public static int hordeSpawnInterval;
	public static int hordeStartTime;
	public static int hordeSpawnDays;
	public static boolean spawnFirstDay;
	public static boolean canSleepDuringHorde;
	public static int eventNotifyMode;
	public static int eventNotifyDuration;
	public static boolean hordeSpawnSound;
	public static String[] hordeSpawnList;
	
	//infection
	public static boolean infectVillagers;
	
	//load config properties
	public static void syncConfig() {
		TheHordes.logInfo("Trying to load config");
		try{
			config.load();
			//horde event
			hordeSpawnAmount = config.get("Horde Spawn Event", "spawnAmount", 25, "Amount of mobs to spawn per wave.").getInt();
			hordeSpawnMultiplier = config.get("Horde Spawn Event", "hordeSpawnMultiplier", 1.1, "Multiplier by which the spawn amount increases by each time the event naturally spawns. (Set to 1 to disable scaling.)").getDouble();
			hordeSpawnDuration = config.get("Horde Spawn Event", "hordeSpawnDuration", 6000, "Time in ticks the spawn lasts for.").getInt();
			hordeSpawnInterval = config.get("Horde Spawn Event", "hordeSpawnInterval", 1000, "Time in ticks between spawns for the horde spawn event.").getInt();
			hordeStartTime = config.get("Horde Spawn Event", "hordeStartTime", 18000, "What time of day does the horde event start? eg 18000 is midnight.").getInt();
			hordeSpawnDays = config.get("Horde Spawn Event", "hordeSpawnDays", 10, "Amount of days between horde spawns").getInt();
			hordeSpawnMax = config.get("Horde Spawn Event", "hordeSpawnMax", 120, "Max cap for the number of entities that can exist from the horde at once.").getInt();
			spawnFirstDay = config.get("Horde Spawn Event", "spawnFirstDay", false, "Set to true to enable the horde spawning on the first day.").getBoolean();
			canSleepDuringHorde = config.get("Horde Spawn Event", "canSleepDuringHorde", false, "Set to false to disable the use of beds during a horde event.").getBoolean();
			eventNotifyMode = config.get("Horde Spawn Event", "eventNotifyMode", 1, "How do players get notified of a horde event. 0: Off, 1: Chat, 2:Action Bar, 3:Title").getInt();
			eventNotifyDuration = config.get("Horde Spawn Event", "eventNotifyDuration", 60, "How long in ticks does the horde notification appear? (Only applies to modes 2 and 3)").getInt();
			hordeSpawnSound = config.get("Horde Spawn Event", "hordeSpawnSound", true, "Play a sound when a horde wave spawns.").getBoolean();
			hordeSpawnList = config.get("Horde Spawn Event", "spawnList",
					new String[]{"minecraft:zombie-20-0-20", "minecraft:zombie_villager-5-0-10", "minecraft:husk-20-30-0"}, 
					"A list of entities to spawn followed by the spawn weight then the day they first appear on then the last day. Higher weight is more common. Leave the last value to 0 to set no max day, entities can have nbt attached to them.(e.g. minecraft:zombie-20-0-0)").getStringList();
			//infection
			infectVillagers = config.get("Infection", "infectVillagers", true, "Can villagers be infected.").getBoolean();
		} catch(Exception e) {
		} finally {
	    	if (config.hasChanged()) config.save();
		}
		
	}

}
