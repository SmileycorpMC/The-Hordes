package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;

public class EntityNBTGetter<T extends Comparable<T>> extends NBTGetter<T> {

	private EntityNBTGetter(ValueGetter<String> value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected CompoundTag getNBT(Level level, LivingEntity entity, RandomSource rand) {
		return NbtPredicate.getEntityTagToCompare(entity);
	}
	
	public static <T extends Comparable<T>> ValueGetter deserialize(JsonObject object, DataType<T> type) {
		try {
			if (object.has("value")) return new EntityNBTGetter<T>(ValueGetter.readValue(DataType.STRING, object.get("value")), type);
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hordes:entity_nbt", e);
		}
		return null;
	}

}
