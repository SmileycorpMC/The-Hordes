package net.smileycorp.hordes.common.data.values;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;

public class EntityNBTGetter<T extends Comparable<T>> extends NBTGetter<T> {

	public EntityNBTGetter(String value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected CompoundTag getNBT(Level level, LivingEntity entity, RandomSource rand) {
		return entity.saveWithoutId(new CompoundTag());
	}

}
