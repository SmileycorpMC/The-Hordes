package net.smileycorp.hordes.config;

import net.minecraftforge.common.config.Configuration;

public class ZombiePlayersConfig {
    
    public static boolean zombieGraves;
    public static boolean zombiePlayersFireImmune;
    public static boolean zombiePlayersBurn;
    public static boolean zombiePlayersOnlyHurtByPlayers;
    public static boolean zombiePlayersDespawnPeaceful;
    
    public static void syncConfig(Configuration config) {
        zombieGraves = config.get("Misc", "zombieGraves", false, "Whether to use zombie players as graves all the time. (Even if infection is disabled)").getBoolean();
        zombiePlayersFireImmune = config.get("Misc", "zombiePlayersFireImmune", false, "Whether zombie players should be immune to fire damage").getBoolean();
        zombiePlayersBurn = config.get("Misc", "zombiePlayersBurn", false, "Whether zombie players burn in sunlight.").getBoolean();
        zombiePlayersOnlyHurtByPlayers = config.get("Misc", "zombiePlayersOnlyHurtByPlayers", false, "Whether zombie players are immune to all damage from non player sources.").getBoolean();
        zombiePlayersDespawnPeaceful = config.get("Misc", "zombiePlayersDespawnPeaceful", false, "Do zombie players despawn in peaceful mode?").getBoolean();
    }
    
    
}
