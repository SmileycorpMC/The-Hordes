package net.smileycorp.hordes.config;

import net.minecraftforge.common.config.Configuration;

public class InfectionConfig {
    //infection
    public static boolean enableMobInfection;
    public static boolean infectPlayers;
    public static double playerInfectionResistance;
    public static int ticksForEffectStage;
    public static boolean infectSlowness;
    public static boolean infectHunger;
    public static boolean infectionSpawnsZombiePlayers;
    public static boolean infectionEntitiesAggroConversions;
    public static double effectStageTickReduction;
    
    //load config properties
    public static void syncConfig(Configuration config) {
        enableMobInfection = config.get("Infection", "enableMobInfection", true, "Set to false to completely disable mob infection and anything related to it.").getBoolean();
        infectPlayers = config.get("Infection", "infectPlayers", true, "Can players be infected.").getBoolean();
        infectSlowness = config.get("Infection", "infectSlowness", true, "Whether later levels of infected should slightly slow movement speed? ").getBoolean();
        infectHunger = config.get("Infection", "infectHunger", true, "Whether later levels of infected should depleet hunger quicker? ").getBoolean();
        playerInfectionResistance = config.get("Infection", "playerInfectionResistance", 0.25, "Base infection resistance of players, corresponds to the percentage to not get infected.").getDouble();
        ticksForEffectStage = config.get("Infection", "ticksForEffectStage", 6000, "How long do each of the 4 effect phases last for before the next phase is activated?").getInt();
        infectionSpawnsZombiePlayers = config.get("Infection", "infectionSpawnsZombiePlayers", true, "Do players who die to infection spawn a zombie??").getBoolean();
        effectStageTickReduction = config.get("Infection", "effectStageTickReduction", 0.95, "What factor should the infection potion effect timer be multiplied by for each cured infection? (Resets on death, set to 1 to disable scaling)").getDouble();
    }
    
}
