package net.smileycorp.hordes.client;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.smileycorp.hordes.common.Hordes;

import java.awt.*;
import java.util.List;


public class ClientConfigHandler {

	public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec config;

	private static TextColor hordeMessageColour = null;
	private static Color zombiePlayerColour = null;
	private static Color drownedPlayerColour = null;

	//horde event
	public static ConfigValue<Integer> eventNotifyMode;
	public static ConfigValue<Integer> eventNotifyDuration;
	public static ConfigValue<Boolean> hordeSpawnSound;
	private static ConfigValue<List<? extends Integer>> configHordeMessageColour;
	private static ConfigValue<List<? extends Integer>> configZombiePlayerColour;
	private static ConfigValue<List<? extends Integer>> configDrownedPlayerColour;

	//infection
	public static ConfigValue<Boolean> playerInfectionVisuals;
	public static ConfigValue<Boolean> playerInfectSound;

	//load config properties
	static {
		Hordes.logInfo("Trying to load client config");
		//horde event
		builder.push("Horde Event");
		eventNotifyMode = builder.comment("How do players get notified of a horde event. 0: Off, 1: Chat, 2:Action Bar, 3:Title").define("eventNotifyMode", 2);
		eventNotifyDuration = builder.comment("How long in ticks does the horde notification appear? (Only applies to modes 2 and 3)").define("eventNotifyDuration", 60);
		hordeSpawnSound = builder.comment("Play a sound when a horde wave spawns.").define("hordeSpawnSound", true);
		configHordeMessageColour = builder.comment("Colour of horde notification messages in the rgb format.")
				.defineList("hordeMessageColour", Lists.newArrayList(135, 0, 0), (x) -> (int)x >= 0 && (int)x < 256);
		configZombiePlayerColour = builder.comment("Colour tint for zombie players.")
				.defineList("zombiePlayerColour", Lists.newArrayList(121, 156, 101), (x) -> (int)x >= 0 && (int)x < 256);
		configDrownedPlayerColour = builder.comment("Colour tint for drowned players.")
				.defineList("drownedPlayerColour", Lists.newArrayList(144, 255, 255), (x) -> (int)x >= 0 && (int)x < 256);
		//infection
		builder.pop();
		builder.push("Infection");
		playerInfectionVisuals = builder.comment("Tint the player's screen and display other visual effects if they are infected.").define("playerInfectionVisuals", true);
		playerInfectSound = builder.comment("Play a sound when the player beomes infected.").define("playerInfectSound", true);
		builder.pop();
		config = builder.build();
	}

	public static TextColor getHordeMessageColour() {
		if (hordeMessageColour == null) {
			List<? extends Integer> rgb = configHordeMessageColour.get();
			if (rgb.size() >= 3) hordeMessageColour = TextColor.fromRgb((rgb.get(0) << 16) + (rgb.get(1) << 8) + rgb.get(2));
			else hordeMessageColour = TextColor.fromRgb(0);
		}
		return hordeMessageColour;
	}

	public static Color getZombiePlayerColour() {
		if (zombiePlayerColour == null) {
			List<? extends Integer> rgb = configZombiePlayerColour.get();
			if (rgb.size() >= 3) zombiePlayerColour = new Color(rgb.get(0), rgb.get(1), + rgb.get(2));
			else zombiePlayerColour = new Color(121, 156, 101);
		}
		return zombiePlayerColour;
	}

	public static Color getDrownedPlayerColour() {
		if (drownedPlayerColour == null) {
			List<? extends Integer> rgb = configDrownedPlayerColour.get();
			if (rgb.size() >= 3) drownedPlayerColour = new Color(rgb.get(0), rgb.get(1), + rgb.get(2));
			else drownedPlayerColour = new Color(144, 255, 255);
		}
		return drownedPlayerColour;
	}

}
