package net.smileycorp.hordes.common;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class Constants {

	public static final String MODID = "hordes";
	public static final String NAME = "The Hordes";

	public static final String hordeEventStart = "message.hordes.EventStart";
	public static final String hordeEventEnd = "message.hordes.EventEnd";
	public static final String hordeTrySleep = "message.hordes.TrySleep";
	public static final String deathMessage = "message.hordes.DeathMessage";
	public static final String deathMessageFighting = "message.hordes.DeathMessageFighting";
	public static final String deathMessageOther = "message.hordes.DeathMessageOther";

	public static final ResourceLocation HORDE_SOUND = loc("horde_spawn");

	public static String name(String name) {
		return name(MODID, name);
	}

	public static String name(String modid, String name) {
		return modid + "." + name.replace("_", "");
	}

	public static ResourceLocation loc(String name) {
		return new ResourceLocation(MODID, name.toLowerCase(Locale.US));
	}

	public static String locStr(String string) {
		return loc(string).toString();
	}

}
