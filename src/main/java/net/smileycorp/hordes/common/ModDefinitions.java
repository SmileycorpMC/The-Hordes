package net.smileycorp.hordes.common;

import net.minecraft.util.ResourceLocation;

public class ModDefinitions {

	public static final String modid = "hordes";
	public static final String name = "The Hordes";
	public static final String version = "1.1.0";
	public static final String dependencies = "required-after:atlaslib@1.1.5";
	public static final String location = "net.smileycorp.hordes.";
	public static final String client = location + "client.ClientProxy";
	public static final String server = location + "common.CommonProxy";
	
	public static final String hordeEventStart = "message.hordes.EventStart";
	public static final String hordeEventEnd = "message.hordes.EventEnd";
	public static final String hordeTrySleep = "message.hordes.TrySleep";
	
	public static String getName(String name) {
		return getName(modid, name);
	}
	
	public static String getName(String modid, String name) {
		return modid + "." + name.replace("_", "");
	}
	
	public static ResourceLocation getResource(String name) {
		return new ResourceLocation(modid, name.toLowerCase());
	}

	public static String getResourceName(String string) {
		return getResource(string).toString();
	}

}
