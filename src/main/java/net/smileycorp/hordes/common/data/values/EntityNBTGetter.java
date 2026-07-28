package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

public class EntityNBTGetter<T extends Comparable<T>> extends NBTGetter<T> {

	private EntityNBTGetter(ValueGetter<String> value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected CompoundTag getNBT(HordeContext<? extends HordePlayerEvent> ctx) {
		return NbtPredicate.getEntityTagToCompare(ctx.getEntity());
	}
	
	public static <T extends Comparable<T>> EntityNBTGetter<T> deserialize(JsonObject object, DataType<T> type) {
		try {
			if (object.has("value")) return new EntityNBTGetter<>(ValueGetter.readValue(DataType.STRING, object.get("value")), type);
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hordes:entity_nbt", e);
		}
		return null;
	}

}
