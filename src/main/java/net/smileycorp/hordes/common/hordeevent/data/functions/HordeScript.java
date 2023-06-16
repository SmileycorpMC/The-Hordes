package net.smileycorp.hordes.common.hordeevent.data.functions;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.event.HordeBuildSpawntableEvent;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.common.hordeevent.data.HordeDataRegistry;
import net.smileycorp.hordes.common.hordeevent.data.conditions.Condition;
import net.smileycorp.hordes.common.hordeevent.data.values.ValueGetter;

import java.util.List;

public class HordeScript<T extends HordePlayerEvent> {

	protected final HordeFunction<T> func;
	protected final Class<T> type;
	private final Condition[] conditions;

	private HordeScript(HordeFunction<T> func, Class<T> type, Condition... conditions) {
		this.func = func;
		this.type = type;
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
			HordeFunction<? extends HordePlayerEvent> function = null;
			ValueGetter getter = ValueGetter.readValue(DataType.STRING, obj.get("value"));
			Class<? extends HordePlayerEvent> clazz = null;
			if (obj.get("function").getAsString().equals("hordes:set_spawntable")) {
				function = new SetSpawntableFunction(getter);
				clazz = HordeBuildSpawntableEvent.class;
			}
			List<Condition> conditions = Lists.newArrayList();
			for (JsonElement condition : obj.get("conditions").getAsJsonArray()) {
				conditions.add(HordeDataRegistry.readCondition(condition.getAsJsonObject()));
			}
			if (function == null || clazz == null) throw new Exception("invalid function: " + obj.get("function").getAsString());
			return new HordeScript(function, clazz, conditions.toArray(new Condition[]{}));
		} catch (Exception e) {
			Hordes.logError("Error loading script " + key + " " + e.getCause() + " " + e.getMessage(), e);
		}
		return null;
	}

}
