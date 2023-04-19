package net.smileycorp.hordes.common;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;


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
	public static ConfigValue<Boolean> infectionSpawnsZombiePlayers;
	public static ConfigValue<List<String>> infectionEntities;
	public static ConfigValue<List<String>> cureItemList;
	public static ConfigValue<List<String>> infectionConversionList;
	public static ConfigValue<Boolean> infectionEntitiesAggroConversions;
	public static ConfigValue<Double> effectStageTickReduction;

	//misc
	public static ConfigValue<Boolean> zombieGraves;
	public static ConfigValue<Boolean> drownedGraves;
	public static ConfigValue<Boolean> drownedPlayers;
	public static ConfigValue<Boolean> zombiePlayersFireImmune;
	public static ConfigValue<Boolean> zombiePlayersBurn;
	public static ConfigValue<Boolean> zombiePlayersOnlyHurtByPlayers;
	public static ConfigValue<Boolean> zombiesBurn;
	public static ConfigValue<Boolean> skeletonsBurn;
	public static ConfigValue<Boolean> zombieVillagersCanBeCured;
	public static ConfigValue<Boolean> aggressiveZombieHorses;
	public static ConfigValue<Boolean> zombieHorsesBurn;
	public static ConfigValue<Boolean> skeletonHorsesBurn;
	public static ConfigValue<Boolean> zombiesScareHorses;

	//load config properties
	static {
		Hordes.logInfo("Trying to load common config");
		//horde event
		builder.push("Horde Event");
		enableHordeEvent = builder.comment("Set to false to completely disable the horde event and anything relating to it.").define("enableHordeEvent", true);
		hordeSpawnAmount = builder.comment("Amount of mobs to spawn per wave.").define("spawnAmount", 25);
		hordeSpawnMultiplier = builder.comment("Multiplier by which the spawn amount increases by each time the event naturally spawns. (Set to 1 to disable scaling.)").define("hordeSpawnMultiplier", 1.05);
		hordeSpawnDuration = builder.comment("Time in ticks the event lasts for").define("hordeSpawnDuration", 6000);
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
				+ "Leave the last value to 0 to set no max day, entities can have nbt attached to them.(e.g. minecraft:zombie{ActiveEffects:[{Id:12,Amplifier:0,Duration:10000}]}-20-0-0)")
				.define("spawnList", Lists.newArrayList("minecraft:zombie-35-0-20", "minecraft:zombie_villager-1-0-20", "minecraft:husk-40-30-0", "minecraft:drowned{HandItems:[{id:trident,Count:1}]}-1-40-0", "minecraft:zombie_horse-3-30-0"));
		//infection
		builder.pop();
		builder.push("Infection");
		enableMobInfection = builder.comment("Set to false to completely disable mob infection and anything related to it.").define("enableMobInfection", true);
		infectVillagers = builder.comment("Can villagers be infected.").define("infectVillagers", true);
		villagerInfectChance = builder.comment("Chance out of 100 for a villager to get infected").define("villagerInfectChance", 85);
		infectPlayers = builder.comment("Can players be infected.").define("infectPlayers", true);
		infectSlowness = builder.comment("Whether later levels of infected should slightly slow movement speed? ").define("infectSlowness", true);
		infectHunger = builder.comment("Whether later levels of infected should depleet hunger quicker? ").define("infectHunger", true);
		playerInfectChance = builder.comment("Chance out of 100 for a player to get infected").define("playerInfectChance", 75);
		ticksForEffectStage = builder.comment("How long do each of the 4 effect phases last for before the next phase is activated?").define("ticksForEffectStage", 6000);
		infectionSpawnsZombiePlayers = builder.comment("Do players who die to infection spawn a zombie?").define("infectionSpawnsZombiePlayers", true);
		infectionEntities = builder.comment("Mobs in this list can cause the infection effect.)")
				.define("infectionEntities", Lists.newArrayList("minecraft:zombie", "minecraft:zombie_villager", "minecraft:husk", "minecraft:drowned", "minecraft:zombie_horse", "hordes:zombie_player", "hordes:drowned_player"));
		cureItemList = builder.comment("A list of items which can cure infection when 'consumed' or used on an entity can accept nbt tags. eg.minecraft:golden_apple, minecraft:potion{Potion: \\\"minecraft:strong_regeneration\\\"}")
				.define("cureItemList", Lists.newArrayList("minecraft:golden_apple", "minecraft:enchanted_golden_apple"));
		infectionConversionList = builder.comment("A list of entities that can be infected, followed by the chance out of 100 to infect, then the entity to convert them to, " +
				"entities can have nbt attached to them.(e.g. minecraft:villager-85-minecraft:zombie_villager{ActiveEffects:[{Id:12,Amplifier:0,Duration:10000}]}), "
				+ "note: players and villagers have special code accociated with them, and should not be in this list")
				.define("customConversionList", Lists.newArrayList("minecraft:horse-65-minecraft:zombie_horse"));
		infectionEntitiesAggroConversions = builder.comment("Do entities on the infectionEntities list automatically target entities on the infectionConversionList").define("infectionEntitiesAggroConversions", true);
		effectStageTickReduction = builder.comment("What factor should the infection potion effect timer be multiplied by for each cured infection? (Resets on death, set to 1 to disable scaling)").define("effectStageTickReduction", 0.95);

		//misc
		builder.pop();
		builder.push("Misc");
		zombieGraves = builder.comment("Whether to use zombie players as graves all the time. (Even if infection is disabled)").define("zombieGraves", false);
		drownedGraves = builder.comment("Whether to always spawn a drowned if a player dies underwater. (Even if infection or zombieGraves are disabled)").define("drownedGraves", false);
		drownedPlayers = builder.comment("Whether to spawn drowned players when a player dies underwater instead of a zombie player. (Whether the zombie is spawned from infection or zombieGraves being true)").define("drownedPlayers", true);
		zombiePlayersFireImmune = builder.comment("Whether zombie players and drowned players should be immune to fire damage").define("zombiePlayersFireImmune", false);
		zombiePlayersBurn = builder.comment("Whether zombie players and drowned players burn in sunlight.").define("zombiePlayersBurn", false);
		zombiePlayersOnlyHurtByPlayers = builder.comment("Whether zombie players and drowned players are immune to all damage from non player sources.").define("zombiePlayersOnlyHurtByPlayers", false);
		zombiesBurn = builder.comment("Whether zombies and drowneds burn in sunlight.").define("zombiesBurn", true);
		skeletonsBurn = builder.comment("Whether skeletons and strays burn in sunlight.").define("skeletonsBurn", true);
		zombieVillagersCanBeCured = builder.comment("Whether zombie villagers have vanilla curing mechanics or not").define("zombieVillagersCanBeCured", false);
		aggressiveZombieHorses = builder.comment("Whether zombie horses are aggressive or not.").define("aggressiveZombieHorses", true);
		zombieHorsesBurn = builder.comment("Whether zombie horses burn in sunlight.").define("zombieHorsesBurn", true);
		skeletonHorsesBurn = builder.comment("Whether skeleton horses burn in sunlight.").define("skeletonHorsesBurn", true);
		zombiesScareHorses = builder.comment("Whether unmounted horses are scared of zombies.").define("zombiesScareHorses", true);
		builder.pop();
		config = builder.build();
	}


}
