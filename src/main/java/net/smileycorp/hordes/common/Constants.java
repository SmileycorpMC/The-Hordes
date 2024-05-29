package net.smileycorp.hordes.common;

import net.minecraft.util.ResourceLocation;

public class Constants {

	public static final String MODID = "hordes";
	public static final String NAME = "The Hordes";
	public static final String VERSION = "1.1.7";
	public static final String DEPENDENCIES = "required-after:atlaslib@1.1.5;after:baubles;after:galacticraft;before:deathchest;before:corpse;before:jei";
	public static final ResourceLocation HORDE_SOUND = loc("horde_spawn");
	private static final String PACKAGE = "net.smileycorp.hordes.";
	public static final String CLIENT_PROXY = PACKAGE + "client.ClientProxy";
	public static final String SERVER_PROXY = PACKAGE + "common.CommonProxy";

	public static final String hordeEventStart = "message.hordes.EventStart";
	public static final String hordeEventEnd = "message.hordes.EventEnd";
	public static final String hordeTrySleep = "message.hordes.TrySleep";
	public static final String deathMessage = "message.hordes.DeathMessage";
	public static final String deathMessageFighting = "message.hordes.DeathMessageFighting";
	public static final String deathMessageOther = "message.hordes.DeathMessageOther";

	public static String name(String name) {
		return name(MODID, name);
	}

	public static String name(String modid, String name) {
		return modid + "." + name.replace("_", "");
	}

	public static ResourceLocation loc(String name) {
		return new ResourceLocation(MODID, name.toLowerCase());
	}

	public static String locName(String string) {
		return loc(string).toString();
	}

}
