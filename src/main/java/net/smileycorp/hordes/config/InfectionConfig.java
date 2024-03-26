package net.smileycorp.hordes.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class InfectionConfig {
    
    public static ForgeConfigSpec.ConfigValue<Boolean> enableMobInfection;
    public static ForgeConfigSpec.ConfigValue<Boolean> infectVillagers;
    public static ForgeConfigSpec.ConfigValue<Boolean> infectPlayers;
    public static ForgeConfigSpec.ConfigValue<Double> villagerInfectChance;
    public static ForgeConfigSpec.ConfigValue<Double> playerInfectChance;
    public static ForgeConfigSpec.ConfigValue<Integer> ticksForEffectStage;
    public static ForgeConfigSpec.ConfigValue<Boolean> infectSlowness;
    public static ForgeConfigSpec.ConfigValue<Boolean> infectHunger;
    public static ForgeConfigSpec.ConfigValue<Boolean> infectionSpawnsZombiePlayers;
    public static ForgeConfigSpec.ConfigValue<Boolean> infectionEntitiesAggroConversions;
    public static ForgeConfigSpec.ConfigValue<Double> effectStageTickReduction;
    
    static void build(ForgeConfigSpec.Builder builder) {
        builder.push("Infection");
        enableMobInfection = builder.comment("Set to false to completely disable mob infection and anything related to it.").define("enableMobInfection", true);
        infectVillagers = builder.comment("Can villagers be infected.").define("infectVillagers", true);
        villagerInfectChance = builder.comment("Chance for a villager to get infected, a value of 1 or higher makes it guaranteed").define("villagerInfectChance", 0.85);
        infectPlayers = builder.comment("Can players be infected.").define("infectPlayers", true);
        infectSlowness = builder.comment("Whether later levels of infected should slightly slow movement speed? ").define("infectSlowness", true);
        infectHunger = builder.comment("Whether later levels of infected should deplete hunger quicker? ").define("infectHunger", true);
        playerInfectChance = builder.comment("Chance for a player to get infected, a value of 1 or higher makes it guaranteed").define("playerInfectChance", 0.75);
        ticksForEffectStage = builder.comment("How long do each of the 4 effect phases last for before the next phase is activated?").define("ticksForEffectStage", 6000);
        infectionSpawnsZombiePlayers = builder.comment("Do players who die to infection spawn a zombie?").define("infectionSpawnsZombiePlayers", true);
        infectionEntitiesAggroConversions = builder.comment("Do entities on the infectionEntities list automatically target entities on the infectionConversionList").define("infectionEntitiesAggroConversions", true);
        effectStageTickReduction = builder.comment("What factor should the infection potion effect timer be multiplied by for each cured infection? (Resets on death, set to 1 to disable scaling)").define("effectStageTickReduction", 0.95);
        builder.pop();
    }
    
}
