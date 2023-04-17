package net.smileycorp.hordes.common.hordeevent.data.conditions;

import java.util.Random;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.ComparableOperation;
import net.smileycorp.hordes.common.hordeevent.data.values.ValueGetter;

public class ComparisonCondition<T extends Comparable<T>> implements Condition {

	protected final ValueGetter<T> value1;
	protected final ComparableOperation operation;
	protected final ValueGetter<T> value2;

	public ComparisonCondition(ValueGetter<T> value1, ComparableOperation operation, ValueGetter<T> value2) {
		this.value1 = value1;
		this.operation = operation;
		this.value2 = value2;
	}

	@Override
	public boolean apply(Level level, Player player, Random rand) {
		return operation.apply(value1.get(level, player, rand), value2.get(level, player, rand));
	}

}
