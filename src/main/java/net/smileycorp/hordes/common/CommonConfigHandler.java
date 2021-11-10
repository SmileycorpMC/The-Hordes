package net.smileycorp.hordes.common;

import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import com.google.common.collect.Lists;


public class CommonConfigHandler {

	public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec config;

	//horde event
	public static ConfigValue<Boolean> enableHordeEvent;
	public static ConfigValue<Integer> hordeSpawnAmount;
	public static ConfigValue<Double> hordeSpawnMultiplier;
	public static ConfigValue<Integer> hordeSpawnMax;
	public static ConfigValue<Integer> hordeSpawnDuration;
	public static ConfigValue<Integer> hordeSpawnInterval;
	public static ConfigValue<Integer> hordeStartTime;
	public static ConfigValue<Integer> hordeSpawnDays;
	public static ConfigValue<Integer> hordeSpawnVariation;
	public static ConfigValue<Integer> dayLength;
	public static ConfigValue<Boolean> spawnFirstDay;
	public static ConfigValue<Boolean> canSleepDuringHorde;
	public static ConfigValue<Double> hordeMultiplayerScaling;
	public static ConfigValue<Boolean> pauseEventServer;
	public static ConfigValue<List<String>> hordeSpawnList;

	//infection
	public static ConfigValue<Boolean> enableMobInfection;
	public static ConfigValue<Boolean> infectVillagers;
	public static ConfigValue<Integer> villagerInfectChance;
	public static ConfigValue<Boolean> infectPlayers;
	public static ConfigValue<Boolean> infectSlowness;
	public static ConfigValue<Boolean> infectHunger;
	public static ConfigValue<Integer> playerInfectChance;
	public static ConfigValue<Integer> ticksForEffectStage;
	public static ConfigValue<List<String>> infectionEntities;
	public static ConfigValue<List<String>> cureItemList;

	//misc
	public static ConfigValue<Boolean> zombieGraves;
	public static ConfigValue<Boolean> drownedGraves;
	public static ConfigValue<Boolean> drownedPlayers;

	//load config properties
	static {
		Hordes.logInfo("Trying to load common config");
		//horde event
		builder.push("Horde Event");
		enableHordeEvent = builder.comment("Set to false to completely disable the horde event and anything relating to it.").define("enableHordeEvent", true);
		hordeSpawnAmount = builder.comment("Amount of mobs to spawn per wave.").define("spawnAmount", 25);
		hordeSpawnMultiplier = builder.comment("Multiplier by which the spawn amount increases by each time the event naturally spawns. (Set to 1 to disable scaling.)").define("hordeSpawnMultiplier", 1.1);
		hordeSpawnDuration = builder.comment("Time in ticks the spawn lasts for.").define("hordeSpawnDuration", 6000);
		hordeSpawnInterval = builder.comment("Time in ticks between spawns for the horde spawn event.").define("hordeSpawnInterval", 1000);
		hordeStartTime = builder.comment("What time of day does the horde event start? eg 18000 is midnight with default day length.").define("hordeStartTime", 18000);
		hordeSpawnDays = builder.comment("Amount of days between horde spawns").define("hordeSpawnDays", 10);
		hordeSpawnVariation = builder.comment("Amount of days a horde event can be randomly extended by").define("hordeSpawnVariation", 0);
		hordeSpawnMax = builder.comment("Max cap for the number of entities that can exist from the horde at once.").define("hordeSpawnMax", 160);
		dayLength = builder.comment("Length of a day (use only if you have another day that changes the length of the day/night cycle) Default is 24000").define("dayLength", 24000);
		spawnFirstDay = builder.comment("Set to true to enable the horde spawning on the first day. (Game day 0)").define("spawnFirstDay", false);
		canSleepDuringHorde = builder.comment("Set to false to disable the use of beds during a horde event.").define("canSleepDuringHorde", false);
		hordeMultiplayerScaling = builder.comment("How much should the size of each horde scale down by when multiple players are near each other?").define("hordeMultiplayerScaling", 0.8);
		pauseEventServer = builder.comment("Do the daylight cycle (and active horde events get paused while there are no players online.).").define("pauseEventServer", true);
		hordeSpawnList = builder.comment("A list of entities to spawn followed by the spawn weight then the day they first appear on then the last day. Higher weight is more common. "
				+ "Leave the last value to 0 to set no max day, entities can have nbt attached to them.(e.g. minecraft:zombie-20-0-0{ActiveEffects:[{Id:12,Amplifier:0,Duration:10000}]})")
				.define("spawnList", Lists.newArrayList("minecraft:zombie-20-0-20", "minecraft:zombie_villager-5-0-10", "minecraft:husk-20-30-0"));
		//infection
		builder.push("Infection");
		enableMobInfection = builder.comment("Set to false to completely disable mob infection and anything related to it.").define("enableMobInfection", true);
		infectVillagers = builder.comment("Can villagers be infected.").define("infectVillagers", true);
		villagerInfectChance = builder.comment("Chance out of 100 for a villager to get infected").define("villagerInfectChance", 85);
		infectPlayers = builder.comment("Can players be infected.").define("infectPlayers", true);
		infectSlowness = builder.comment("Whether later levels of infected should slightly slow movement speed? ").define("infectSlowness", true);
		infectHunger = builder.comment("Whether later levels of infected should depleet hunger quicker? ").define("infectHunger", true);
		playerInfectChance = builder.comment("Chance out of 100 for a player to get infected").define("playerInfectChance", 75);
		ticksForEffectStage = builder.comment("How long do each of the 4 effect phases last for before the next phase is activated?").define("ticksForEffectStage", 6000);
		infectionEntities = builder.comment("Mobs which are based on entities in this list can cause the infection effect. (the default zombie covers zombies, husks, zombie villagers and drowned, as well ad the player zombie/drowned)")
				.define("infectionEntities", Lists.newArrayList("minecraft:zombie"));
		cureItemList = builder.comment("A list of items which can cure infection when 'consumed' or used on an entity can accept nbt tags. eg.minecraft:golden_apple, minecraft:potion{Potion: \"minecraft:strong_regeneration\"}")
			.define("cureItemList", Lists.newArrayList("minecraft:golden_apple", "minecraft:enchanted_golden_apple"));
		//misc
		builder.push("Misc");
		zombieGraves = builder.comment("Whether to use zombie players as graves all the time. (Even if infection is disabled)").define("zombieGraves", false);
		drownedGraves = builder.comment("Whether to always spawn a drowned if a player dies underwater. (Even if infection or zombieGraves are disabled)").define("drownedGraves", false);
		drownedPlayers = builder.comment("Whether to spawn drowned players when a player dies underwater instead of a zombie player. (Whether the zombie is spawned from infection or zombieGraves being true)").define("drownedPlayers", true);
		builder.pop();
		config = builder.build();
	}

}
