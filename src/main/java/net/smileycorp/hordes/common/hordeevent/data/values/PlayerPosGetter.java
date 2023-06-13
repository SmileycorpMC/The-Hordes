package net.smileycorp.hordes.common.hordeevent.data.values;

import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;

import java.util.Random;

public class PlayerPosGetter<T extends Comparable<T>, Number> implements ValueGetter<T> {

	private final Axis axis;
	private final DataType<T> type;

	public PlayerPosGetter(String value, DataType<T> type) {
		this.axis = Axis.byName(value);
		this.type = type;
	}

	@Override
	public T get(Level level, Player player, Random rand) {
		if (!type.isNumber()) return null;
		if (type == DataType.INT) return type.cast(player.blockPosition().get(axis));
		return type.cast(player.position().get(axis));
	}
}
