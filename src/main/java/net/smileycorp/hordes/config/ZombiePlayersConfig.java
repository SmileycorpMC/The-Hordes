package net.smileycorp.hordes.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ZombiePlayersConfig {
    
    public static ForgeConfigSpec.ConfigValue<Boolean> zombieGraves;
    public static ForgeConfigSpec.ConfigValue<Boolean> drownedPlayers;
    public static ForgeConfigSpec.ConfigValue<Boolean> huskPlayers;
    public static ForgeConfigSpec.ConfigValue<Boolean> zombiePlayersFireImmune;
    public static ForgeConfigSpec.ConfigValue<Boolean> zombiePlayersBurn;
    public static ForgeConfigSpec.ConfigValue<Boolean> zombiePlayersOnlyHurtByPlayers;
    public static ForgeConfigSpec.ConfigValue<Boolean> zombiePlayersStoreItems;
    
    static void build(ForgeConfigSpec.Builder builder) {
        zombieGraves = builder.comment("Whether to use zombie players as graves all the time. (Even if infection is disabled)").define("zombieGraves", false);
        drownedPlayers = builder.comment("Whether to spawn drowned players when a player dies underwater instead of a zombie player. (Whether the zombie is spawned from infection or zombieGraves being true)").define("drownedPlayers", true);
        huskPlayers = builder.comment("Whether to spawn husk players when a player dies in a desert biome instead of a zombie player. (Whether the zombie is spawned from infection or zombieGraves being true)").define("huskPlayers", true);
        zombiePlayersFireImmune = builder.comment("Whether zombie players, drowned players and husk players should be immune to fire damage").define("zombiePlayersFireImmune", false);
        zombiePlayersBurn = builder.comment("Whether zombie players and drowned players burn in sunlight.").define("zombiePlayersBurn", false);
        zombiePlayersOnlyHurtByPlayers = builder.comment("Whether zombie players, drowned players and husk players are immune to all damage from non player sources.").define("zombiePlayersOnlyHurtByPlayers", false);
        zombiePlayersStoreItems = builder.comment("Whether zombie players, drowned players and husk players store items dropped by the player that spawned them.").define("zombiePlayersStoreItems", true);
    }
}
