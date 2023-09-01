package net.smileycorp.hordes.common.hordeevent.data.scripts.values;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;

import java.util.Random;

public class PlayerNBTGetter<T extends Comparable<T>> extends NBTGetter<T> {

	public PlayerNBTGetter(String value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected CompoundTag getNBT(Level level, Player player, Random rand) {
		return player.saveWithoutId(new CompoundTag());
	}

}
