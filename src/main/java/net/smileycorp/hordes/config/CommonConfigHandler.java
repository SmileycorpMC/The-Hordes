package net.smileycorp.hordes.config;

import net.minecraftforge.common.config.Configuration;
import net.smileycorp.hordes.common.Hordes;

public class CommonConfigHandler {
	static Configuration config;
	
	//misc
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
			HordeEventConfig.syncConfig(config);
			//infection
			InfectionConfig.enableMobInfection = config.get("Infection", "enableMobInfection", true, "Set to false to completely disable mob infection and anything related to it.").getBoolean();
			InfectionConfig.infectVillagers = config.get("Infection", "infectVillagers", true, "Can villagers be infected.").getBoolean();
			InfectionConfig.villagerInfectChance = config.get("Infection", "villagerInfectChance", 85, "Chance out of 100 for a villager to get infected").getInt();
			InfectionConfig.infectPlayers = config.get("Infection", "infectPlayers", true, "Can players be infected.").getBoolean();
			InfectionConfig.infectSlowness = config.get("Infection", "infectSlowness", true, "Whether later levels of infected should slightly slow movement speed? ").getBoolean();
			InfectionConfig.infectHunger = config.get("Infection", "infectHunger", true, "Whether later levels of infected should depleet hunger quicker? ").getBoolean();
			InfectionConfig.playerInfectChance = config.get("Infection", "playerInfectChance", 75, "Chance out of 100 for a player to get infected").getInt();
			InfectionConfig.ticksForEffectStage = config.get("Infection", "ticksForEffectStage", 6000, "How long do each of the 4 effect phases last for before the next phase is activated?").getInt();
			InfectionConfig.infectionSpawnsZombiePlayers = config.get("Infection", "infectionSpawnsZombiePlayers", true, "Do players who die to infection spawn a zombie??").getBoolean();
			ClientConfigHandler.playerInfectionVisuals = config.get("Infection", "playerInfectionVisuals", true, "Tint the player's screen and display other visual effects if they are infected.").getBoolean();
			ClientConfigHandler.playerInfectSound = config.get("Infection", "playerInfectSound", true, "Play a sound when the player beomes infected.").getBoolean();
			InfectionConfig.infectionEntities = config.get("Infection", "infectionEntities",
					new String[]{"minecraft:zombie", "minecraft:zombie_villager", "minecraft:husk", "minecraft:zombie_horse", "hordes:zombie_player"},
					"Mobs which are based on entities in this list can cause the infection effect.").getStringList();
			InfectionConfig.cureItemList = config.get("Infection", "cureItemList",
					new String[]{"minecraft:golden_apple:*"},
					"A list of items which can cure infection when 'consumed' use '*' to specify any metadata, can accept nbt tags. eg.minecraft:golden_apple:*, minecraft:potion{Potion: \\\"minecraft:strong_regeneration\\\"}").getStringList();
			InfectionConfig.infectionConversionList = config.get("Infection", "infectionConversionList",
					new String[]{"minecraft:horse-65-minecraft:zombie_horse"},
					"A list of entities that can be infected, followed by the chance out of 100 to infect, then the entity to convert them to, " +
							"entities can have nbt attached to them.(e.g. minecraft:villager-85-minecraft:zombie_villager{ActiveEffects:[{Id:12,Amplifier:0,Duration:10000}]}), "
							+ "note: players and villagers have special code accociated with them, and should not be in this list").getStringList();
			InfectionConfig.infectionEntitiesAggroConversions = config.get("Infection", "infectionEntitiesAggroConversions", true, "Do entities on the infectionEntities list automatically target entities on the infectionConversionList").getBoolean();
			InfectionConfig.effectStageTickReduction = config.get("Infection", "effectStageTickReduction", 0.95, "What factor should the infection potion effect timer be multiplied by for each cured infection? (Resets on death, set to 1 to disable scaling)").getDouble();

			//misc
			ZombiePlayersConfig.zombieGraves = config.get("Misc", "zombieGraves", false, "Whether to use zombie players as graves all the time. (Even if infection is disabled)").getBoolean();
			ZombiePlayersConfig.zombiePlayersFireImmune = config.get("Misc", "zombiePlayersFireImmune", false, "Whether zombie players should be immune to fire damage").getBoolean();
			ZombiePlayersConfig.zombiePlayersBurn = config.get("Misc", "zombiePlayersBurn", false, "Whether zombie players burn in sunlight.").getBoolean();
			ZombiePlayersConfig.zombiePlayersOnlyHurtByPlayers = config.get("Misc", "zombiePlayersOnlyHurtByPlayers", false, "Whether zombie players are immune to all damage from non player sources.").getBoolean();
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
