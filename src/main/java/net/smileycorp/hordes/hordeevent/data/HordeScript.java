package net.smileycorp.hordes.hordeevent.data;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.common.data.conditions.Condition;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.functions.FunctionRegistry;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;
import net.smileycorp.hordes.hordeevent.data.functions.NestedHordeFunction;
import net.smileycorp.hordes.hordeevent.data.functions.universal.MultipleFunction;

import java.util.List;

public class HordeScript<T extends HordePlayerEvent> {

	protected final HordeFunction<T> func;
	protected final Class<T> type;
	private final ResourceLocation name;
	private final Condition[] conditions;

	private HordeScript(HordeFunction<T> func, Class<T> type, ResourceLocation name, Condition... conditions) {
		this.func = func;
		this.type = type;
		this.name = name;
		this.conditions = conditions;
	}

	public void apply(HordeContext<T> ctx) {
		func.apply(ctx);
	}

	public Class<T> getType() {
		return type;
	}
	
	public ResourceLocation getName() {
		return name;
	}

	public boolean shouldApply(HordeContext<? extends HordePlayerEvent> ctx) {
		for (Condition condition : conditions)  if (!condition.apply(ctx)) return false;
		return true;
	}

	public static HordeScript<?> deserialize(ResourceLocation key, JsonElement json) {
		try {
			if (json instanceof JsonArray) {
				NestedHordeFunction<?> function = MultipleFunction.deserialize(json.getAsJsonArray());
				if (function == null) return null;
				return new HordeScript(function, function.getEventClass(), key);
			}
			JsonObject obj = json.getAsJsonObject();
			Pair<Class<HordePlayerEvent>, HordeFunction<HordePlayerEvent>> pair = FunctionRegistry.readFunction(obj);
			Class<? extends HordePlayerEvent> clazz = pair.getFirst();
			HordeFunction<? extends HordePlayerEvent> function = pair.getSecond();
			if (function == null || clazz == null) throw new Exception("invalid function: " + obj.get("function").getAsString());
			List<Condition> conditions = Lists.newArrayList();
			if (obj.has("conditions")) obj.get("conditions").getAsJsonArray().forEach(condition ->
					conditions.add(DataRegistry.readCondition(condition.getAsJsonObject())));
			return new HordeScript(function, clazz,  key, conditions.toArray(new Condition[]{}));
		} catch (Exception e) {
			HordesLogger.logError("Error loading script " + key + " " + e.getCause() + " " + e.getMessage(), e);
		}
		return null;
	}
	
}
