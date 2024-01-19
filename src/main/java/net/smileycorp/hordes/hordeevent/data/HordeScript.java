package net.smileycorp.hordes.hordeevent.data;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.common.data.conditions.Condition;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordeBuildSpawntableEvent;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.functions.FunctionRegistry;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;
import net.smileycorp.hordes.hordeevent.data.functions.SetSpawntableFunction;

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

	public void apply(T event) {
		func.apply(event);
	}

	public Class<T> getType() {
		return type;
	}

	public boolean shouldApply(Level level, Player player, RandomSource rand) {
		for (Condition condition : conditions)  if (!condition.apply(level, player, rand)) return false;
		return true;
	}

	public static HordeScript deserialize(ResourceLocation key, JsonElement value) {
		try {
			JsonObject obj = value.getAsJsonObject();
			Pair<Class<? extends HordePlayerEvent>, HordeFunction<? extends HordePlayerEvent>> pair = FunctionRegistry.readFunction(obj);
			Class<? extends HordePlayerEvent> clazz = pair.getFirst();
			HordeFunction<? extends HordePlayerEvent> function = pair.getSecond();
			if (function == null || clazz == null) throw new Exception("invalid function: " + obj.get("function").getAsString());
			List<Condition> conditions = Lists.newArrayList();
			for (JsonElement condition : obj.get("conditions").getAsJsonArray()) conditions.add(DataRegistry.readCondition(condition.getAsJsonObject()));
			return new HordeScript(function, clazz,  key, conditions.toArray(new Condition[]{}));
		} catch (Exception e) {
			HordesLogger.logError("Error loading script " + key + " " + e.getCause() + " " + e.getMessage(), e);
		}
		return null;
	}

	public ResourceLocation getName() {
		return name;
	}
}
