package net.smileycorp.hordes.client;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.smileycorp.hordes.common.HordesLogger;

import java.awt.*;
import java.util.List;


public class ClientConfigHandler {

	public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec config;

	private static TextColor hordeMessageColour = null;
	private static Color zombiePlayerColour = null;
	private static Color drownedPlayerColour = null;
	private static Color huskPlayerColour = null;
	private static Color hordeEventSkyColour = null;
	private static Color hordeEventMoonColour = null;

	//horde event
	public static ConfigValue<Integer> eventNotifyMode;
	public static ConfigValue<Integer> eventNotifyDuration;
	public static ConfigValue<Boolean> hordeSpawnSound;
	private static ConfigValue<List<? extends Integer>> configHordeMessageColour;
	public static ConfigValue<Boolean> hordeEventTintsSky;
	public static ConfigValue<List<? extends Integer>> configHordeEventSkyColour;
	public static ConfigValue<List<? extends Integer>> configHordeEventMoonColour;


	//infection
	public static ConfigValue<Boolean> playerInfectionVisuals;
	public static ConfigValue<Boolean> playerInfectSound;
	public static ConfigValue<Boolean> infectionProtectSound;
	private static ConfigValue<List<? extends Integer>> configZombiePlayerColour;
	private static ConfigValue<List<? extends Integer>> configDrownedPlayerColour;
	private static ConfigValue<List<? extends Integer>> configHuskPlayerColour;

	//load config properties
	static {
		HordesLogger.logInfo("Trying to load client config");
		//horde event
		builder.push("Horde Event");
		eventNotifyMode = builder.comment("How do players get notified of a horde event. 0: Off, 1: Chat, 2:Action Bar, 3:Title").define("eventNotifyMode", 2);
		eventNotifyDuration = builder.comment("How long in ticks does the horde notification appear? (Only applies to modes 2 and 3)").define("eventNotifyDuration", 60);
		hordeSpawnSound = builder.comment("Play a sound when a horde wave spawns.").define("hordeSpawnSound", true);
		configHordeMessageColour = builder.comment("Colour of horde notification messages in the rgb format.")
				.defineList("hordeMessageColour", Lists.newArrayList(135, 0, 0), (x) -> (int)x >= 0 && (int)x < 256);
		hordeEventTintsSky = builder.comment("Whether the sky and moon should be tinted on a horde night").define("hordeEventTintsSky", true);
		configHordeEventSkyColour = builder.comment("Colour of horde notification messages in the rgb format.")
				.defineList("hordeEventSkyColour", Lists.newArrayList(102, 0, 0), (x) -> (int)x >= 0 && (int)x < 256);
		configHordeEventMoonColour = builder.comment("Colour of horde notification messages in the rgb format.")
				.defineList("hordeEventMoonColour", Lists.newArrayList(193, 57, 15), (x) -> (int)x >= 0 && (int)x < 256);

		//infection
		builder.pop();
		builder.push("Infection");
		playerInfectionVisuals = builder.comment("Tint the player's screen and display other visual effects if they are infected.").define("playerInfectionVisuals", true);
		playerInfectSound = builder.comment("Play a sound when the player becomes infected?").define("playerInfectSound", true);
		infectionProtectSound = builder.comment("Play a sound when infection gets prevented?").define("infectionProtectSound", false);
		configZombiePlayerColour = builder.comment("Colour tint for zombie players.")
				.defineList("zombiePlayerColour", Lists.newArrayList(121, 156, 101), (x) -> (int)x >= 0 && (int)x < 256);
		configDrownedPlayerColour = builder.comment("Colour tint for drowned players.")
				.defineList("drownedPlayerColour", Lists.newArrayList(144, 255, 255), (x) -> (int)x >= 0 && (int)x < 256);
		configHuskPlayerColour = builder.comment("Colour tint for husk players.")
				.defineList("huskPlayerColour", Lists.newArrayList(193, 168, 5), (x) -> (int)x >= 0 && (int)x < 256);
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

	public static Color getHordeSkyColour() {
		if (hordeEventSkyColour == null) {
			List<? extends Integer> rgb = configHordeEventSkyColour.get();
			if (rgb.size() >= 3) hordeEventSkyColour = new Color(rgb.get(0), rgb.get(1), + rgb.get(2));
			else hordeEventSkyColour = new Color(102, 0, 0);
		}
		return hordeEventSkyColour;
	}

	public static Color getHordeMoonColour() {
		if (hordeEventMoonColour == null) {
			List<? extends Integer> rgb = configHordeEventMoonColour.get();
			if (rgb.size() >= 3) hordeEventMoonColour = new Color(rgb.get(0), rgb.get(1), + rgb.get(2));
			else hordeEventMoonColour = new Color(193, 57, 15);
		}
		return hordeEventMoonColour;
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

	public static Color getHuskPlayerColour() {
		if (huskPlayerColour == null) {
			List<? extends Integer> rgb = configHuskPlayerColour.get();
			if (rgb.size() >= 3) huskPlayerColour = new Color(rgb.get(0), rgb.get(1), + rgb.get(2));
			else huskPlayerColour = new Color(193, 168, 5);
		}
		return huskPlayerColour;
	}

}
