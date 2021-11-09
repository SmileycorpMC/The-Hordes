package net.smileycorp.hordes.client;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.smileycorp.hordes.common.Hordes;


public class ClientConfigHandler {

	public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec config;

	//horde event
	public static ConfigValue<Integer> eventNotifyMode;
	public static ConfigValue<Integer> eventNotifyDuration;
	public static ConfigValue<Boolean> hordeSpawnSound;

	//infection
	public static ConfigValue<Boolean> playerInfectionVisuals;
	public static ConfigValue<Boolean> playerInfectSound;

	//load config properties
	static {
		Hordes.logInfo("Trying to load client config");
		//horde event
		builder.push("Horde Event");
		eventNotifyMode = builder.comment("How do players get notified of a horde event. 0: Off, 1: Chat, 2:Action Bar, 3:Title").define("eventNotifyMode", 1);
		eventNotifyDuration = builder.comment("How long in ticks does the horde notification appear? (Only applies to modes 2 and 3)").define("eventNotifyDuration", 60);
		hordeSpawnSound = builder.comment("Play a sound when a horde wave spawns.").define("hordeSpawnSound", true);
		//infection
		builder.push("Infection");
		playerInfectionVisuals = builder.comment("Tint the player's screen and display other visual effects if they are infected.").define("playerInfectionVisuals", true);
		playerInfectSound = builder.comment("Play a sound when the player beomes infected.").define("playerInfectSound", true);
		builder.pop();
		config = builder.build();
	}

}
