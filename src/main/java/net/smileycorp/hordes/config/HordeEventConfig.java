package net.smileycorp.hordes.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class HordeEventConfig {
    
    public static ForgeConfigSpec.ConfigValue<Boolean> enableHordeEvent;
    public static ForgeConfigSpec.ConfigValue<Boolean> hordesCommandOnly;
    public static ForgeConfigSpec.ConfigValue<Integer> hordeSpawnAmount;
    public static ForgeConfigSpec.ConfigValue<Double> hordeSpawnMultiplier;
    public static ForgeConfigSpec.ConfigValue<Integer> hordeSpawnMax;
    public static ForgeConfigSpec.ConfigValue<Integer> hordeSpawnDuration;
    public static ForgeConfigSpec.ConfigValue<Integer> hordeSpawnInterval;
    public static ForgeConfigSpec.ConfigValue<Integer> hordeStartTime;
    public static ForgeConfigSpec.ConfigValue<Integer> hordeSpawnDays;
    public static ForgeConfigSpec.ConfigValue<Integer> hordeSpawnVariation;
    public static ForgeConfigSpec.ConfigValue<Integer> dayLength;
    public static ForgeConfigSpec.ConfigValue<Integer> hordePathingInterval;
    public static ForgeConfigSpec.ConfigValue<Double> hordeEntitySpeed;
    public static ForgeConfigSpec.ConfigValue<Boolean> spawnFirstDay;
    public static ForgeConfigSpec.ConfigValue<Boolean> canSleepDuringHorde;
    public static ForgeConfigSpec.ConfigValue<Double> hordeMultiplayerScaling;
    public static ForgeConfigSpec.ConfigValue<Boolean> pauseEventServer;
    public static ForgeConfigSpec.ConfigValue<Boolean> hordeEventByPlayerTime;
    public static ForgeConfigSpec.ConfigValue<Integer> hordeStartBuffer;
    
    static void build(ForgeConfigSpec.Builder builder) {
        builder.push("Horde Event");
        enableHordeEvent = builder.comment("Set to false to completely disable the horde event and anything relating to it.").define("enableHordeEvent", true);
        hordesCommandOnly = builder.comment("Set to true to disable natural horde spawns (hordes can only be spawned with commands).").define("hordesCommandOnly", false);
        hordeSpawnAmount = builder.comment("Amount of mobs to spawn per wave.").define("spawnAmount", 25);
        hordeSpawnMultiplier = builder.comment("Multiplier by which the spawn amount increases by each time the event naturally spawns. (Set to 1 to disable scaling.)").define("hordeSpawnMultiplier", 1.05);
        hordeSpawnDuration = builder.comment("Time in ticks the event lasts for").define("hordeSpawnDuration", 6000);
        hordeSpawnInterval = builder.comment("Time in ticks between spawns for the horde spawn event.").define("hordeSpawnInterval", 1000);
        hordeStartTime = builder.comment("What time of day does the horde event start? eg 18000 is midnight with default day length.").define("hordeStartTime", 18000);
        hordeSpawnDays = builder.comment("Amount of days between horde spawns.").define("hordeSpawnDays", 10);
        hordeSpawnVariation = builder.comment("Amount of days a horde event can be randomly delayed by").define("hordeSpawnVariation", 0);
        hordeSpawnMax = builder.comment("Max cap for the number of entities that can exist from the horde at once.").define("hordeSpawnMax", 160);
        dayLength = builder.comment("Length of a day (use only if you have another day that changes the length of the day/night cycle) Default is 24000").define("dayLength", 24000);
        hordePathingInterval = builder.comment("How many ticks does the horde pathing ai take before recalculating? (Increase this if you are having server slowdown during horde events.)").define("hordePathingInterval", 10);
        hordeEntitySpeed = builder.comment("How fast do horde mobs move towards their tracked player?").define("hordeEntitySpeed", 1d);
        spawnFirstDay = builder.comment("Set to true to enable the horde spawning on the first day. (Game day 0)").define("spawnFirstDay", false);
        canSleepDuringHorde = builder.comment("Set to false to disable the use of beds during a horde event.").define("canSleepDuringHorde", false);
        hordeMultiplayerScaling = builder.comment("How much should the size of each horde scale down by when multiple players are near each other?").define("hordeMultiplayerScaling", 0.8);
        pauseEventServer = builder.comment("Do the daylight cycle (and active horde events get paused while there are no players online.).").define("pauseEventServer", true);
        hordeEventByPlayerTime = builder.comment("Are horde events tracked by player play time instead of world time.").define("hordeEventByPlayerTime", true);
        hordeStartBuffer = builder.comment("How many ticks after a hordes scheduled time can it start?").define("hordeStartBuffer", 1200);
        builder.pop();
    }
    
}
