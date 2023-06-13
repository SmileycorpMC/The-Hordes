package net.smileycorp.hordes.common.hordeevent.data.conditions;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.LogicalOperation;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.data.DataRegistry;

public class LogicalCondition implements Condition {

	protected final LogicalOperation operation;
	protected final Condition[] conditions;

	private LogicalCondition(LogicalOperation operation, Condition... conditions) {
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

	public static LogicalCondition deserialize(LogicalOperation operation, JsonElement json) {
		try {
			List<Condition> conditions = Lists.newArrayList();
			for (JsonElement element : json.getAsJsonArray()) {
				try {
					conditions.add(DataRegistry.readCondition(element.getAsJsonObject()));
				} catch(Exception e) {
					Hordes.logError("Failed to read condition of logical " + element, e);
				}
			}
			return new LogicalCondition(operation, conditions.toArray(new Condition[]{}));
		} catch(Exception e) {
			Hordes.logError("Incorrect parameters for condition hordes:"+operation.getName(), e);
		}
		return null;
	}

}
