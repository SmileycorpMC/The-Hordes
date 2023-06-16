package net.smileycorp.hordes.common.data.values;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.NBTExplorer;

public abstract class NBTGetter<T extends Comparable<T>> implements ValueGetter<T> {

	protected final NBTExplorer<T> explorer;

	public NBTGetter(String value, DataType<T> type) {
		explorer = new NBTExplorer<T>(value, type);
	}

	@Override
	public T get(Level level, LivingEntity entity, RandomSource rand) {
		try {
			return explorer.findValue(getNBT(level, entity, rand));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected abstract CompoundTag getNBT(Level level, LivingEntity entity, RandomSource rand);

}
