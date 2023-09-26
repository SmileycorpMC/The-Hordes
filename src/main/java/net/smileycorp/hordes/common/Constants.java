package net.smileycorp.hordes.common;

import net.minecraft.resources.ResourceLocation;

public class Constants {

	public static final String MODID = "hordes";
	public static final String NAME = "The Hordes";

	public static final String hordeEventStart = "message.hordes.EventStart";
	public static final String hordeEventEnd = "message.hordes.EventEnd";
	public static final String hordeTrySleep = "message.hordes.TrySleep";

	public static final ResourceLocation HORDE_SOUND = loc("horde_spawn");

	public static String name(String name) {
		return name(MODID, name);
	}

	public static String name(String modid, String name) {
		return modid + "." + name.replace("_", "");
	}

	public static ResourceLocation loc(String name) {
		return new ResourceLocation(MODID, name.toLowerCase());
	}

	public static String locStr(String string) {
		return loc(string).toString();
	}

}
