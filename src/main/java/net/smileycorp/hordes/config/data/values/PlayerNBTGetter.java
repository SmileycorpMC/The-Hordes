package net.smileycorp.hordes.config.data.values;

import com.google.gson.JsonObject;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.config.data.DataType;

import java.util.Random;

public class PlayerNBTGetter<T extends Comparable<T>> extends NBTGetter<T> {

	private PlayerNBTGetter(ValueGetter<String> value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected NBTTagCompound getNBT(World level, EntityLivingBase entity, EntityPlayerMP player, Random rand) {
		return CommandBase.entityToNBT(player);
	}
	
	public static <T extends Comparable<T>> ValueGetter deserialize(JsonObject object, DataType<T> type) {
		try {
			if (object.has("value")) return new PlayerNBTGetter<T>(ValueGetter.readValue(DataType.STRING, object.get("value")), type);
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hordes:player_nbt", e);
		}
		return null;
	}

}
