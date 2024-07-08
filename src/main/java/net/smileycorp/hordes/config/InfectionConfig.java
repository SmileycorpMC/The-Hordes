package net.smileycorp.hordes.config;

import net.minecraftforge.common.config.Configuration;

public class InfectionConfig {
    //infection
    public static boolean enableMobInfection;
    public static boolean infectVillagers;
    public static double villagerInfectChance;
    public static boolean infectPlayers;
    public static double playerInfectChance;
    public static int ticksForEffectStage;
    public static boolean infectSlowness;
    public static boolean infectHunger;
    public static boolean infectionSpawnsZombiePlayers;
    public static boolean infectionEntitiesAggroConversions;
    public static double effectStageTickReduction;
    
    //load config properties
    public static void syncConfig(Configuration config) {
        enableMobInfection = config.get("Infection", "enableMobInfection", true, "Set to false to completely disable mob infection and anything related to it.").getBoolean();
        infectVillagers = config.get("Infection", "infectVillagers", true, "Can villagers be infected.").getBoolean();
        villagerInfectChance = config.get("Infection", "villagerInfectChance", 0.85, "Chance for a villager to get infected, a value of 1 or higher makes it guaranteed").getDouble();
        infectPlayers = config.get("Infection", "infectPlayers", true, "Can players be infected.").getBoolean();
        infectSlowness = config.get("Infection", "infectSlowness", true, "Whether later levels of infected should slightly slow movement speed? ").getBoolean();
        infectHunger = config.get("Infection", "infectHunger", true, "Whether later levels of infected should depleet hunger quicker? ").getBoolean();
        playerInfectChance = config.get("Infection", "playerInfectChance", 0.75, "Chance for a player to get infected, a value of 1 or higher makes it guaranteed").getDouble();
        ticksForEffectStage = config.get("Infection", "ticksForEffectStage", 6000, "How long do each of the 4 effect phases last for before the next phase is activated?").getInt();
        infectionSpawnsZombiePlayers = config.get("Infection", "infectionSpawnsZombiePlayers", true, "Do players who die to infection spawn a zombie??").getBoolean();
        effectStageTickReduction = config.get("Infection", "effectStageTickReduction", 0.95, "What factor should the infection potion effect timer be multiplied by for each cured infection? (Resets on death, set to 1 to disable scaling)").getDouble();
    }
    
}
