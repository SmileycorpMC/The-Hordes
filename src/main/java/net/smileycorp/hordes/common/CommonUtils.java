package net.smileycorp.hordes.common;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;

public class CommonUtils {

	public static NBTTagCompound parseNBT(String name, String nbtstring) {
		NBTTagCompound nbt = null;
		try {
			NBTTagCompound parsed = JsonToNBT.getTagFromJson(nbtstring);
			if (parsed != null) nbt = parsed;
			else throw new NullPointerException("Parsed NBT is null.");
		} catch (Exception e) {
			Hordes.logError("Error parsing nbt for entity " + name + " " + e.getMessage(), e);
		}
		return nbt;
	}

}
