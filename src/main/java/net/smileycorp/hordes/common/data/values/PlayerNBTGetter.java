package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.NBTPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;

import java.util.Random;

public class PlayerNBTGetter<T extends Comparable<T>> extends NBTGetter<T> {

	private PlayerNBTGetter(ValueGetter<String> value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected CompoundNBT getNBT(World level, LivingEntity entity, ServerPlayerEntity player, Random rand) {
		return NBTPredicate.getEntityTagToCompare(player);
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
