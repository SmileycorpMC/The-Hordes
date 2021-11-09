package net.smileycorp.hordes.common;

import net.minecraft.util.ResourceLocation;

public class ModDefinitions {

	public static final String MODID = "hordes";
	public static final String NAME = "The Hordes";

	public static final String hordeEventStart = "message.hordes.EventStart";
	public static final String hordeEventEnd = "message.hordes.EventEnd";
	public static final String hordeTrySleep = "message.hordes.TrySleep";
	public static final String deathMessage = "message.hordes.DeathMessage";
	public static final String deathMessageFighting = "message.hordes.DeathMessageFighting";
	public static final String deathMessageOther = "message.hordes.DeathMessageOther";

	public static final ResourceLocation HORDE_SOUND = getResource("horde_spawn");

	public static String getName(String name) {
		return getName(MODID, name);
	}

	public static String getName(String modid, String name) {
		return modid + "." + name.replace("_", "");
	}

	public static ResourceLocation getResource(String name) {
		return new ResourceLocation(MODID, name.toLowerCase());
	}

	public static String getResourceName(String string) {
		return getResource(string).toString();
	}

}
