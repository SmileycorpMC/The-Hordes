package net.smileycorp.hordes.hordeevent.data.values;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

public class PlayerNBTGetter<T extends Comparable<T>> extends NBTGetter<T> {

	private PlayerNBTGetter(ValueGetter<String> value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected CompoundTag getNBT(HordePlayerEvent event) {
		return NbtPredicate.getEntityTagToCompare(event.getPlayer());
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
