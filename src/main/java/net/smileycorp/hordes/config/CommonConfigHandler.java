package net.smileycorp.hordes.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.smileycorp.hordes.common.HordesLogger;


public class CommonConfigHandler {

	public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec config;
	
	//misc
	public static ConfigValue<Boolean> zombiesBurn;
	public static ConfigValue<Boolean> skeletonsBurn;
	public static ConfigValue<Boolean> zombieVillagersCanBeCured;
	public static ConfigValue<Boolean> piglinsHoglinsConvert;
	public static ConfigValue<Boolean> aggressiveZombieHorses;
	public static ConfigValue<Boolean> zombieHorsesBurn;
	public static ConfigValue<Boolean> skeletonHorsesBurn;
	public static ConfigValue<Boolean> zombiesScareHorses;
	public static ConfigValue<Boolean> aggressiveZombiePiglins;
	public static ConfigValue<Boolean> piglinsHuntZombies;
	public static ConfigValue<Boolean> piglinsCureThemself;
	public static ConfigValue<Boolean> zoglinsAttackUndead;

	//load config properties
	static {
		HordesLogger.logInfo("Trying to load common config");
		HordeEventConfig.build(builder);
		InfectionConfig.build(builder);
		builder.push("Misc");
		zombiesBurn = builder.comment("Whether zombies and drowned burn in sunlight.").define("zombiesBurn", false);
		skeletonsBurn = builder.comment("Whether skeletons and strays burn in sunlight.").define("skeletonsBurn", false);
		zombieVillagersCanBeCured = builder.comment("Whether zombie villagers have vanilla curing mechanics or not").define("zombieVillagersCanBeCured", false);
		piglinsHoglinsConvert = builder.comment("Whether piglins and hoglins automatically convert to zombies in the overworld").define("piglinsHoglinsConvert", false);
		aggressiveZombieHorses = builder.comment("Whether zombie horses are aggressive or not.").define("aggressiveZombieHorses", true);
		zombieHorsesBurn = builder.comment("Whether zombie horses burn in sunlight.").define("zombieHorsesBurn", false);
		skeletonHorsesBurn = builder.comment("Whether skeleton horses burn in sunlight.").define("skeletonHorsesBurn", false);
		zombiesScareHorses = builder.comment("Whether unmounted horses are scared of zombies.").define("zombiesScareHorses", true);
		aggressiveZombiePiglins = builder.comment("Whether zombie piglins are hostile by default").define("zombiePiglinsHostile", true);
		piglinsHuntZombies = builder.comment("Whether piglins kill zombie mobs").define("piglinsHuntZombies", true);
		piglinsCureThemself = builder.comment("Whether piglins use cures they find and keep in their inventory to heal infection.").define("piglinsCureThemself", true);
		zoglinsAttackUndead = builder.comment("Whether zoglins are agressive towards other undead mobs").define("zoglinsAttackUndead", false);
		ZombiePlayersConfig.build(builder);
		builder.pop();
		config = builder.build();
	}

}
