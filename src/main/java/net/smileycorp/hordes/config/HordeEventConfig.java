package net.smileycorp.hordes.config;

import net.minecraftforge.common.config.Configuration;

public class HordeEventConfig {
    
    //horde event
    public static boolean enableHordeEvent;
    public static boolean hordesCommandOnly;
    public static int hordeSpawnAmount;
    public static double hordeSpawnMultiplier;
    public static int hordeSpawnMax;
    public static int hordeSpawnDuration;
    public static int hordeSpawnInterval;
    public static int hordeStartTime;
    public static int hordeSpawnDays;
    public static int hordeSpawnVariation;
    public static int dayLength;
    public static int hordePathingInterval;
    public static double hordeEntitySpeed;
    public static boolean spawnFirstDay;
    public static boolean canSleepDuringHorde;
    public static double hordeMultiplayerScaling;
    public static boolean pauseEventServer;
    public static boolean hordeEventByPlayerTime;
    public static int hordeStartBuffer;
    public static int hordeSpawnChecks;
    
    public static void syncConfig(Configuration config) {
        enableHordeEvent = config.get("Horde Spawn Event", "enableHordeEvent", true, "Set to false to completely disable the horde event and anything relating to it.").getBoolean();
        hordesCommandOnly = config.get("Horde Spawn Event", "hordesCommandOnly", false, "Set to true to disable natural horde spawns (hordes can only be spawned with commands)").getBoolean();
        hordeSpawnAmount = config.get("Horde Spawn Event", "spawnAmount", 25, "Amount of mobs to spawn per wave.").getInt();
        hordeSpawnMultiplier = config.get("Horde Spawn Event", "hordeSpawnMultiplier", 1.1, "Multiplier by which the spawn amount increases by each time the event naturally spawns. (Set to 1 to disable scaling.)").getDouble();
        hordeSpawnDuration = config.get("Horde Spawn Event", "hordeSpawnDuration", 6000, "Time in ticks the spawn lasts for.").getInt();
        hordeSpawnInterval = config.get("Horde Spawn Event", "hordeSpawnInterval", 1000, "Time in ticks between spawns for the horde spawn event.").getInt();
        hordeStartTime = config.get("Horde Spawn Event", "hordeStartTime", 18000, "What time of day does the horde event start? eg 18000 is midnight with default day length.").getInt();
        hordeSpawnDays = config.get("Horde Spawn Event", "hordeSpawnDays", 10, "Amount of days between horde spawns").getInt();
        hordeSpawnVariation = config.get("Horde Spawn Event", "hordeSpawnVariation", 0, "Amount of days a horde event can be randomly extended by").getInt();
        hordeSpawnMax = config.get("Horde Spawn Event", "hordeSpawnMax", 120, "Max cap for the number of entities that can exist from the horde at once.").getInt();
        dayLength = config.get("Horde Spawn Event", "dayLength", 24000, "Length of a day (use only if you have another day that changes the length of the day/night cycle) Default is 24000").getInt();
        hordePathingInterval = config.get("Horde Spawn Event", "hordePathingInterval", 10, "How many ticks does the horde pathing ai take before recalculating? (Increase this if you are having server slowdown during horde events.)").getInt();
        hordeEntitySpeed = config.get("Horde Spawn Event", "hordeEntitySpeed", 1, "How fast do horde mobs move towards their tracked player?").getDouble();
        spawnFirstDay = config.get("Horde Spawn Event", "spawnFirstDay", false, "Set to true to enable the horde spawning on the first day. (Game day 0)").getBoolean();
        canSleepDuringHorde = config.get("Horde Spawn Event", "canSleepDuringHorde", false, "Set to false to disable the use of beds during a horde event.").getBoolean();
        hordeMultiplayerScaling = config.get("Horde Spawn Event", "hordeMultiplayerScaling", 0.8, "How much should the size of each horde scale down by when multiple players are near each other?").getDouble();
        pauseEventServer = config.get("Horde Spawn Event", "pauseEventServer", true, "Do the daylight cycle (and active horde events get paused while there are no players online.).").getBoolean();
        hordeEventByPlayerTime = config.get("Horde Spawn Event", "hordeEventByPlayerTime", true, "Are horde events tracked by player play time instead of world time.").getBoolean();
        hordeStartBuffer = config.get("Horde Spawn Event", "hordeStartBuffer", 1200, "How many ticks after a hordes scheduled time can it start?").getInt();
        hordeSpawnChecks = config.get("Horde Spawn Event", "hordeSpawnChecks", 25, "How many attempts should horde events make to avoid spawning mobs in light areas or outside their spawn type.").getInt();
    }
    
}
