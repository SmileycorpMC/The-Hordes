package net.smileycorp.hordes.config.data.values;

import com.dhanantry.scapeandrunparasites.world.SRPSaveData;
import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.config.data.DataType;

import java.util.Random;

public class ParasiteKillGetter implements ValueGetter<Integer> {

	@Override
	public Integer get(World level, EntityLivingBase entity, EntityPlayerMP player, Random rand) {
		return SRPSaveData.get(level).getTotalKills(level.provider.getDimension());
	}
	
	public static <T extends Comparable<T>> ValueGetter deserialize(JsonObject object, DataType<T> type) {
		try {
			return new ParasiteKillGetter();
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hsrparasites:kills", e);
		}
		return null;
	}
	
}
