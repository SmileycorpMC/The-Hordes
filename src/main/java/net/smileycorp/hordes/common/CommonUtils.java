package net.smileycorp.hordes.common;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;

public class CommonUtils {

	public static CompoundNBT parseNBT(String name, String nbtstring) {
		CompoundNBT nbt = null;
		try {
			CompoundNBT parsed = JsonToNBT.parseTag(nbtstring);
			if (parsed != null) nbt = parsed;
			else throw new NullPointerException("Parsed NBT is null.");
		} catch (Exception e) {
			Hordes.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
			Hordes.logError("Error parsing nbt for entity " + name + " " + e.getMessage(), e);
		}
		return nbt;
	}

}
