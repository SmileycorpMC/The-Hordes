package net.smileycorp.hordes.common.hordeevent.data.values;

import java.util.Random;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface ValueGetter<T extends Comparable<T>> {

	public T get(Level level, Player player, Random rand);

}
