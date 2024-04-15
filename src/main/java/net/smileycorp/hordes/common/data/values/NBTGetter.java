package net.smileycorp.hordes.common.data.values;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.NBTExplorer;

import java.util.Random;

public abstract class NBTGetter<T extends Comparable<T>> implements ValueGetter<T> {

	protected final ValueGetter<String> value;
	private final DataType<T> type;
	
	public NBTGetter(ValueGetter<String> value, DataType<T> type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public T get(World level, LivingEntity entity, ServerPlayerEntity player, Random rand) {
		try {
			return new NBTExplorer<>(value.get(level, entity, player, rand), type).findValue(getNBT(level, entity, player, rand));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected abstract CompoundNBT getNBT(World level, LivingEntity entity, ServerPlayerEntity player, Random rand);

}
