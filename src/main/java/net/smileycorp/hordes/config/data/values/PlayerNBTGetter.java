package net.smileycorp.hordes.config.data.values;

import com.google.gson.JsonObject;
import net.minecraft.command.CommandBase;
import net.minecraft.nbt.NBTTagCompound;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;

public class PlayerNBTGetter<T extends Comparable<T>> extends NBTGetter<T> {

	private PlayerNBTGetter(ValueGetter<String> value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected NBTTagCompound getNBT(HordeContext<? extends HordePlayerEvent> ctx) {
		return CommandBase.entityToNBT(ctx.getPlayer());
	}
	
	public static <T extends Comparable<T>> PlayerNBTGetter<T> deserialize(JsonObject object, DataType<T> type) {
		try {
			if (object.has("value")) return new PlayerNBTGetter<>(ValueGetter.readValue(DataType.STRING, object.get("value")), type);
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hordes:player_nbt", e);
		}
		return null;
	}

}
