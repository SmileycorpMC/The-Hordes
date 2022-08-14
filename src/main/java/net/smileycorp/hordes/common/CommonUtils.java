package net.smileycorp.hordes.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

public class CommonUtils {

	public static CompoundTag parseNBT(String name, String nbtstring) {
		CompoundTag nbt = null;
		try {
			CompoundTag parsed = TagParser.parseTag(nbtstring);
			if (parsed != null) nbt = parsed;
			else throw new NullPointerException("Parsed NBT is null.");
		} catch (Exception e) {
			Hordes.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
			Hordes.logError("Error parsing nbt for entity " + name + " " + e.getMessage(), e);
		}
		return nbt;
	}

}
