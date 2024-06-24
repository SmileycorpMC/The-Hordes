package net.smileycorp.hordes.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class InfectionConfig {
    
    public static ModConfigSpec.ConfigValue<Boolean> enableMobInfection;
    public static ModConfigSpec.ConfigValue<Boolean> infectPlayers;
    public static ModConfigSpec.ConfigValue<Double> playerInfectionResistance;
    public static ModConfigSpec.ConfigValue<Integer> ticksForEffectStage;
    public static ModConfigSpec.ConfigValue<Boolean> infectSlowness;
    public static ModConfigSpec.ConfigValue<Boolean> infectHunger;
    public static ModConfigSpec.ConfigValue<Boolean> infectionSpawnsZombiePlayers;
    public static ModConfigSpec.ConfigValue<Boolean> infectionEntitiesAggroConversions;
    public static ModConfigSpec.ConfigValue<Double> effectStageTickReduction;
    
    static void build(ModConfigSpec.Builder builder) {
        builder.push("Infection");
        enableMobInfection = builder.comment("Set to false to completely disable mob infection and anything related to it.").define("enableMobInfection", true);
        infectPlayers = builder.comment("Can players be infected.").define("infectPlayers", true);
        infectSlowness = builder.comment("Whether later levels of infected should slightly slow movement speed? ").define("infectSlowness", true);
        infectHunger = builder.comment("Whether later levels of infected should deplete hunger quicker? ").define("infectHunger", true);
        playerInfectionResistance = builder.comment("Base infection resistance of players, corresponds to the percentage to not get infected.").define("playerInfectionResistance", 0.25);
        ticksForEffectStage = builder.comment("How long do each of the 4 effect phases last for before the next phase is activated?").define("ticksForEffectStage", 6000);
        infectionSpawnsZombiePlayers = builder.comment("Do players who die to infection spawn a zombie?").define("infectionSpawnsZombiePlayers", true);
        infectionEntitiesAggroConversions = builder.comment("Do entities on the infectionEntities list automatically target entities on the infectionConversionList").define("infectionEntitiesAggroConversions", true);
        effectStageTickReduction = builder.comment("What factor should the infection potion effect timer be multiplied by for each cured infection? (Resets on death, set to 1 to disable scaling)").define("effectStageTickReduction", 0.95);
        builder.pop();
    }
    
}
