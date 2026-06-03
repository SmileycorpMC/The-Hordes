package net.smileycorp.hordes.config;

import net.minecraftforge.common.config.Configuration;

public class ZombiePlayersConfig {
    
    public static boolean zombieGraves;
    public static boolean huskPlayers;
    public static boolean drownedPlayers;
    public static boolean zombiePlayersFireImmune;
    public static boolean zombiePlayersBurn;
    public static boolean zombiePlayersOnlyHurtByPlayers;
    public static boolean zombiePlayersStoreItems;
    public static boolean zombiePlayersDespawnPeaceful;
    
    public static void syncConfig(Configuration config) {
        zombieGraves = config.get("Zombie Players", "zombieGraves", false, "Whether to use zombie players as graves all the time. (Even if infection is disabled)").getBoolean();
        huskPlayers = config.get("Zombie Players", "huskPlayers", true, "Whether to spawn husk players when a player dies in a desert biome instead of a zombie player. (Whether the zombie is spawned from infection or zombieGraves being true)").getBoolean();
        huskPlayers = config.get("Zombie Players", "drownedPlayers", true, "(Oceanic Expanse Support) Whether to spawn drowned players when a player dies underwater instead of a zombie player. (Whether the zombie is spawned from infection or zombieGraves being true)").getBoolean();
        zombiePlayersFireImmune = config.get("Zombie Players", "zombiePlayersFireImmune", false, "Whether zombie players should be immune to fire damage").getBoolean();
        zombiePlayersBurn = config.get("Zombie Players", "zombiePlayersBurn", false, "Whether zombie players burn in sunlight.").getBoolean();
        zombiePlayersOnlyHurtByPlayers = config.get("Zombie Players", "zombiePlayersOnlyHurtByPlayers", false, "Whether zombie players are immune to all damage from non player sources.").getBoolean();
        zombiePlayersStoreItems = config.get("Zombie Players", "zombiePlayersStoreItems", true, "Whether zombie players, drowned players and husk players store items dropped by the player that spawned them.").getBoolean();
        zombiePlayersDespawnPeaceful = config.get("Zombie Players", "zombiePlayersDespawnPeaceful", false, "Do zombie players despawn in peaceful mode?").getBoolean();
    }
    
    
}
