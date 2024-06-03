package net.smileycorp.hordes.config.data.values;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.config.data.DataType;

import java.util.Random;

public class LevelNBTGetter<T extends Comparable<T>> extends NBTGetter<T> {

	private LevelNBTGetter(ValueGetter<String> value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected NBTTagCompound getNBT(World level, EntityLivingBase entity, EntityPlayerMP player, Random rand)  {
		WorldInfo info = level.getWorldInfo();
		NBTTagCompound nbt = info.cloneNBTCompound(new NBTTagCompound());
		FMLCommonHandler.instance().handleWorldDataSave((SaveHandler) level.getSaveHandler(), info, nbt);
		return nbt;
	}
	
	public static <T extends Comparable<T>> ValueGetter deserialize(JsonObject object, DataType<T> type) {
		try {
			if (object.has("value")) return new LevelNBTGetter<T>(ValueGetter.readValue(DataType.STRING, object.get("value")), type);
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hordes:level_nbt", e);
		}
		return null;
	}
	
}
