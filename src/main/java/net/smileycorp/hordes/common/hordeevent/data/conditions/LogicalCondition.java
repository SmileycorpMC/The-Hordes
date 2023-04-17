package net.smileycorp.hordes.common.hordeevent.data.conditions;

import java.util.Random;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.LogicalOperation;

public class LogicalCondition implements Condition {

	protected final LogicalOperation operation;
	protected final Condition[] conditions;

	public LogicalCondition(LogicalOperation operation, Condition... conditions) {
		this.operation = operation;
		this.conditions = conditions;
	}

	@Override
	public boolean apply(Level level, Player player, Random rand) {
		boolean result = false;
		for (Condition condition : conditions) {
			result = operation.apply(result, condition.apply(level, player, rand));
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < conditions.length; i++) {
			builder.append(conditions[i].toString());
			if (i < conditions.length-1) builder.append(" " + operation.getSymbol() + " ");
		}
		return super.toString() + "[" + builder.toString() + "]";
	}

}
