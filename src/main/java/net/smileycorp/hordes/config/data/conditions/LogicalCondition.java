package net.smileycorp.hordes.config.data.conditions;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.LogicalOperation;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.common.data.LogicalOperation;
import net.smileycorp.hordes.config.data.DataRegistry;

import java.util.List;
import java.util.Random;

public class LogicalCondition implements Condition {

	protected final LogicalOperation operation;
	protected final Condition[] conditions;

	private LogicalCondition(LogicalOperation operation, Condition... conditions) {
		this.operation = operation;
		this.conditions = conditions;
	}

	@Override
	public boolean apply(World level, EntityLiving entity, EntityPlayerMP player, Random rand) {
		boolean result = false;
		for (Condition condition : conditions) result = operation.apply(result, condition.apply(level, entity, player, rand));
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < conditions.length; i++) {
			builder.append(conditions[i].toString());
			if (i < conditions.length-1) builder.append(" " + operation.getSymbol() + " ");
		}
		return super.toString() + "[" + builder + "]";
	}

	public static LogicalCondition deserialize(LogicalOperation operation, JsonElement json) {
		try {
			List<Condition> conditions = Lists.newArrayList();
			for (JsonElement element : json.getAsJsonArray()) {
				try {
					conditions.add(DataRegistry.readCondition(element.getAsJsonObject()));
				} catch(Exception e) {
					HordesLogger.logError("Failed to read condition of logical " + element, e);
				}
			}
			return new LogicalCondition(operation, conditions.toArray(new Condition[]{}));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:"+operation.getName(), e);
		}
		return null;
	}

}
