package net.smileycorp.hordes.common.hordeevent.data.values;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.NBTExplorer;

import java.util.Random;

public abstract class NBTGetter<T extends Comparable<T>> implements ValueGetter<T> {

	protected final NBTExplorer<T> explorer;

	public NBTGetter(String value, DataType<T> type) {
		explorer = new NBTExplorer<T>(value, type);
	}

	@Override
	public T get(Level level, Player player, Random rand) {
		try {
			return explorer.findValue(getNBT(level, player, rand));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected abstract CompoundTag getNBT(Level level, Player player, Random rand);

}
