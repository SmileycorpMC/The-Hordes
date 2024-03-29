package net.smileycorp.hordes.common.data.values;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.NBTExplorer;

public abstract class NBTGetter<T extends Comparable<T>> implements ValueGetter<T> {

	protected final ValueGetter<String> value;
	private final DataType<T> type;
	
	public NBTGetter(ValueGetter<String> value, DataType<T> type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public T get(Level level, LivingEntity entity, ServerPlayer player, RandomSource rand) {
		try {
			return new NBTExplorer<>(value.get(level, entity, player, rand), type).findValue(getNBT(level, entity, player, rand));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected abstract CompoundTag getNBT(Level level, LivingEntity entity, ServerPlayer player, RandomSource rand);

}
