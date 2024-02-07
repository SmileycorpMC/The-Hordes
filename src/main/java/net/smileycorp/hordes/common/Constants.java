package net.smileycorp.hordes.common;

import net.minecraft.util.ResourceLocation;

public class Constants {

	public static final String modid = "hordes";
	public static final String name = "The Hordes";
	public static final String version = "1.1.7";
	public static final String dependencies = "required-after:atlaslib@1.1.5;after:baubles;after:galacticraft;before:deathchest;before:corpse;before:jei";
	public static final String location = "net.smileycorp.hordes.";
	public static final String client = location + "client.ClientProxy";
	public static final String server = location + "common.CommonProxy";

	public static final String hordeEventStart = "message.hordes.EventStart";
	public static final String hordeEventEnd = "message.hordes.EventEnd";
	public static final String hordeTrySleep = "message.hordes.TrySleep";
	public static final String deathMessage = "message.hordes.DeathMessage";
	public static final String deathMessageFighting = "message.hordes.DeathMessageFighting";
	public static final String deathMessageOther = "message.hordes.DeathMessageOther";

	public static String name(String name) {
		return name(modid, name);
	}

	public static String name(String modid, String name) {
		return modid + "." + name.replace("_", "");
	}

	public static ResourceLocation loc(String name) {
		return new ResourceLocation(modid, name.toLowerCase());
	}

	public static String locName(String string) {
		return loc(string).toString();
	}

}
