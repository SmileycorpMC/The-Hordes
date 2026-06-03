package net.smileycorp.hordes.common;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import java.util.Locale;

public class Constants {

	public static final String MODID = "hordes";
	public static final String NAME = "The Hordes";

	public static final String VERSION = "1.6.3";
	public static final String DEPENDENCIES = "required-after:atlaslib@[1.1.10,);after:baubles;after:galacticraft;before:deathchest;before:corpse;before:jei";

	private static final String PACKAGE = "net.smileycorp.hordes.";
	public static final String CLIENT_PROXY = PACKAGE + "client.ClientProxy";
	public static final String SERVER_PROXY = PACKAGE + "common.CommonProxy";

	public static final String hordeEventStart = "message.hordes.EventStart";
	public static final String hordeEventEnd = "message.hordes.EventEnd";
	public static final String hordeTrySleep = "message.hordes.TrySleep";
	
	public static final ResourceLocation HORDE_SOUND = loc("horde_spawn");
	public static final SoundEvent INFECT_SOUND = sound(loc("infect"));
	public static final SoundEvent IMMUNE_SOUND = sound(loc("immune"));
	
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

	private static SoundEvent sound(ResourceLocation loc) {
		SoundEvent sound = new SoundEvent(loc);
		sound.setRegistryName(loc);
		return sound;
	}

}
