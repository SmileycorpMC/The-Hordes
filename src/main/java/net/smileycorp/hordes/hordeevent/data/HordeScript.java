package net.smileycorp.hordes.hordeevent.data;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.common.data.conditions.Condition;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.functions.FunctionRegistry;
import net.smileycorp.hordes.hordeevent.data.functions.HordeFunction;
import net.smileycorp.hordes.hordeevent.data.functions.MultipleFunction;

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
	
	public ResourceLocation getName() {
		return name;
	}

	public boolean shouldApply(Level level, LivingEntity entity, ServerPlayer player, RandomSource rand) {
		for (Condition condition : conditions)  if (!condition.apply(level, entity, player, rand)) return false;
		return true;
	}
	
	public int sort(HordeScript other) {
		String a = name.toString();
		String b = other.name.toString();
		int ia = 0, ib = 0;
		int nza, nzb;
		char ca, cb;
		int result;
		while (true) {
			nza = nzb = 0;
			ca = charAt(a, ia);
			cb = charAt(b, ib);
			while (ca == '0') {
				if (ca == '0') nza++;
				else nza = 0;
				if (!Character.isDigit(charAt(a, ia + 1))) break;
				ca = charAt(a, ia++);
			}
			while (cb == '0') {
				if (cb == '0') nzb++;
				else nzb = 0;
				if (!Character.isDigit(charAt(b, ib + 1))) break;
				cb = charAt(b, ib++);
			}
			if (Character.isDigit(ca) && Character.isDigit(cb))
				if ((result = compareRight(a.substring(ia), b.substring(ib))) != 0) return result;
			if (ca == 0 && cb == 0) return nza - nzb;
			if (ca < cb) return -1;
			else if (ca > cb) return +1;
			ia++;
			ib++;
		}
	}
	
	private char charAt(String s, int i) {
		return i >= s.length() ? 0 : Character.toUpperCase(s.charAt(i));
	}
	
	private int compareRight(String a, String b) {
		int bias = 0;
		int ia = 0;
		int ib = 0;
		for (;; ia++, ib++) {
			char ca = charAt(a, ia);
			char cb = charAt(b, ib);
			if (!Character.isDigit(ca) && !Character.isDigit(cb)) return bias;
			else if (!Character.isDigit(ca)) return -1;
			else if (!Character.isDigit(cb)) return 1;
			else if (ca < cb) if (bias == 0) bias = -1;
			else if (ca > cb) if (bias == 0) bias = 1;
			else if (ca == 0 && cb == 0) return bias;
		}
	}
	
	public static HordeScript deserialize(ResourceLocation key, JsonElement json) {
		try {
			if (json instanceof JsonArray) {
				Pair<Class<HordePlayerEvent>, HordeFunction<HordePlayerEvent>> pair = MultipleFunction.deserialize(json.getAsJsonArray());
				return new HordeScript(pair.getSecond(), pair.getFirst(), key);
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
