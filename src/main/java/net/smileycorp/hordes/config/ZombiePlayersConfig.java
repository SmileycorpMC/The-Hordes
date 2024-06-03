package net.smileycorp.hordes.config;

import net.minecraftforge.common.config.Configuration;

public class ZombiePlayersConfig {
    
    public static boolean zombieGraves;
    public static boolean zombiePlayersFireImmune;
    public static boolean zombiePlayersBurn;
    public static boolean zombiePlayersOnlyHurtByPlayers;
    public static void syncConfig(Configuration config) {
        ZombiePlayersConfig.zombieGraves = config.get("Misc", "zombieGraves", false, "Whether to use zombie players as graves all the time. (Even if infection is disabled)").getBoolean();
        ZombiePlayersConfig.zombiePlayersFireImmune = config.get("Misc", "zombiePlayersFireImmune", false, "Whether zombie players should be immune to fire damage").getBoolean();
        ZombiePlayersConfig.zombiePlayersBurn = config.get("Misc", "zombiePlayersBurn", false, "Whether zombie players burn in sunlight.").getBoolean();
        ZombiePlayersConfig.zombiePlayersOnlyHurtByPlayers = config.get("Misc", "zombiePlayersOnlyHurtByPlayers", false, "Whether zombie players are immune to all damage from non player sources.").getBoolean();
    }
    
    
}
